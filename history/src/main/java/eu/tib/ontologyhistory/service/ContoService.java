package eu.tib.ontologyhistory.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import eu.tib.ontologyhistory.dto.conto.Difference;
import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.conto.Timeline;
import eu.tib.ontologyhistory.dto.conto.TimelineMessage;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.InvalidContoDiff;
import eu.tib.ontologyhistory.repository.InvalidContoDiffRepository;
import eu.tib.ontologyhistory.repository.InvalidDiffRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.utils.SparqlQueries;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class ContoService {

    private final InvalidContoDiffRepository invalidContoDiffRepository;

    private static final Character UNIQUE_DELIMITER = '\u001f';

    private static final String FUSEKI_DOCKER_CONN_STRING = "http://fuseki:3030/";

    private static final String OUTPUT_FILE = "output.ttl";

    private static final String QUAD_FILE = "all_diffs.nq";

    private static final Path ONTOLOGY_LEFT = Path.of("ontology-left.txt");

    private static final Path ONTOLOGY_RIGHT = Path.of("ontology-right.txt");

    public Set<TempGraph> findAll(String dataset) {
        val graphs = new HashSet<TempGraph>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                Query graphQuery = QueryFactory.create(SparqlQueries.ONDET_PREFIXES + SparqlQueries.GET_ALL_GRAPHS);
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery)) {
                    ResultSet results = qExec.execSelect();
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        RDFNode graph = soln.get("graph");
                        graphs.add(new TempGraph(String.valueOf(graph)));
                    }
                }
            });
        }

        return graphs;
    }

    public List<Timeline> findByUrl(String ontologyURL, String dataset) {
        List<Timeline> timelines = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode commitLabel, message, firstCommitTime;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        commitLabel = soln.get("commit_label");
                        message = soln.get("message");
                        firstCommitTime = soln.get("first_commit_time");
                        timelines.add(new Timeline(
                                Instant.parse(apacheDatetimeToInstant(firstCommitTime.toString())),
                                commitLabel.toString(),
                                message.toString()));
                    }
                }
            });
        }

        timelines.sort(Comparator.comparing(Timeline::firstCommitTime).reversed());
        return timelines.subList(1, timelines.size());
    }

    public List<String> findFirstByUrl(String ontologyURL, String dataset) {
        fusekiAuthenticate();
        val res = new ArrayList<String>();
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.GET_GRAPH_BY_URL);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    if (results.hasNext()) {
                        QuerySolution soln = results.next();
                        RDFNode graph = soln.get("graph");
                        res.add(String.valueOf(graph));
                    }
                }
            });
        }

        return res;
    }

    public Difference timeline(String commitId, String dataset) {

        val invalidDiff = invalidContoDiffRepository.findFirstByParentSha(commitId);
        if (invalidDiff != null) {
            return new Difference(null, invalidDiff.getMessage());
        }

        fusekiAuthenticate();
        List<String> result = new ArrayList<>();
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE_ELEMENT);
                graphQuery.setLiteral("commitId", commitId);
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode ppLabel, s, p, o;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        ppLabel = soln.get("pp_label");
                        s = soln.get("s");
                        p = soln.get("p");
                        o = soln.get("o");
                        String change = ppLabel.toString() + " " + s.toString() + " " + p.toString() + " " + o.toString();
                        result.add(change);
                    }
                }
            });
        }

        return new Difference(result, null);
    }

    public Map<Instant, Collection<TimelineMessage>> timelineMessage(String dataset, String ontologyURL, String resourceUri, String firstCommitTime, String secondCommitTime) {
        fusekiAuthenticate();
        Multimap<Instant, TimelineMessage> result = ArrayListMultimap.create();
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE_MESSAGE);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                graphQuery.setParam("resourceArg", ResourceFactory.createResource(resourceUri));
                graphQuery.setLiteral("firstCommitTime", firstCommitTime);
                graphQuery.setLiteral("secondCommitTime", secondCommitTime);
                try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode ppLabel, commitTime, p, o;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        commitTime = soln.get("first_commit_time");
                        ppLabel = soln.get("pp_label");
                        p = soln.get("p");
                        o = soln.get("o");
                        result.put(Instant.parse(apacheDatetimeToInstant(commitTime.toString())),
                                new TimelineMessage(ppLabel.toString(), p.toString(), o.toString()));
                    }
                }
            });
        }
//        result.sort(Comparator.comparing(TimelineMessage::commitTime).reversed());
        return result.asMap();
    }

    public List<String> operations(String dataset, String ontologyURL) {
        List<String> functions = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.ALL_DISTINCT_OPERATIONS);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode function;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        function = soln.get("function");
                        functions.add(function.toString());
                    }
                }
            });
        }

        return functions;
    }

    public List<String> operationsData(String dataset, String ontologyURL) {
        List<String> subjects = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.DATA_RELATED_TO_OPERATION);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode subject;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        subject = soln.get("s");
                        subjects.add(subject.toString());
                    }
                }
            });
        }

        return subjects;
    }

    public List<Timeline> getVersions(String dataset, String ontologyURL) {
        List<Timeline> subjects = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode message, versionTime, commitLabel;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        commitLabel = soln.get("commit_label");
                        message = soln.get("message");
                        versionTime = soln.get("first_commit_time");
                        subjects.add(new Timeline(
                                Instant.parse(apacheDatetimeToInstant(versionTime.toString())),
                                commitLabel.toString(),
                                message.toString()));
                    }
                }
            });
        }
        subjects.sort(Comparator.comparing(Timeline::firstCommitTime));
        return subjects;
    }

    public List<String> dataInBetweenDates(String dataset, String ontologyURL, String startDatetime, String endDatetime) {
        List<String> subjects = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.DATA_BETWEEN_TWO_DATES);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(ontologyURL));
                graphQuery.setLiteral("startDatetimeArg", startDatetime);
                graphQuery.setLiteral("endDatetimeArg", endDatetime);
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode ppLabel, subject, object, predicate;
                    while (results.hasNext()) {
                        QuerySolution soln = results.nextSolution();
                        ppLabel = soln.get("pp_label");
                        subject = soln.get("s");
                        object = soln.get("o");
                        predicate = soln.get("p");
                        String change = ppLabel.toString() + " " + subject.toString() + " " + predicate.toString() + " " + object.toString();
                        subjects.add(change);
                    }
                }
            });
        }

        return subjects;
    }

    public void remove(String id) {

    }

    public void deleteAll() {

    }

    public void update(String id) {
    }
//    private List<?> runQuery(Query query, String dataset, Object dto) {
//        List<GraphInfo> graphs = new ArrayList<>();
//        GraphInfo graph = new GraphInfo();
//        fusekiAuthenticate();
//
//        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
//        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
//                .destination(datasetServiceUrl);
//
//        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
//            Txn.executeRead(conn, () -> {
//                List<Var> vars = query.getProjectVars();
//                try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, query)) {
//                    ResultSet results = qExec.execSelect();
//                    while (results.hasNext()) {
//                        QuerySolution soln = results.nextSolution();
//                        for (Var var : vars) {
//                            RDFNode rdfNode = soln.get(var.getVarName());
//                            graph = populateGraph(dto, var.getVarName(), rdfNode);
//                        }
//                        graphs.add(graph);
//                    }
//                }
//            });
//        }
//
//        return graphs;
//    }
//
//    private GraphInfo populateGraph(Object object, String var, RDFNode rdfNode) {
//        GraphInfo graph = (GraphInfo) object;
//        Class<?> dtoClass = graph.getClass();
//
//        for (Field field : dtoClass.getDeclaredFields()) {
//            if (field.getName().equals(var)) {
//                field.setAccessible(true);
//                try {
//                    field.set(graph, rdfNode.toString());
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//
//        return graph;
//    }

    private String apacheDatetimeToInstant(String datetime) {
        return datetime.split("\\^\\^")[0];
    }

    public void create(String url, String dataset) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url);
        for (val diffAdd : diffAdds) {
            try {
                diffExecute(diffAdd, url);
                uploadOntologyToFuseki(new File(OUTPUT_FILE), dataset);
                uploadOntologyToFuseki(new File(QUAD_FILE), dataset);
            } catch (Exception e) {
                val invalidContoDiff = InvalidContoDiff.builder()
                        .sha(diffAdd.sha())
                        .parentSha(diffAdd.parentSha())
                        .message(e.getMessage())
                        .build();

                invalidContoDiffRepository.insert(invalidContoDiff);
                log.error(e.getMessage(), e);
            }
        }
    }

    public void create(String url, String dataset, List<DiffAdd> diffAdds) {
        for (val diffAdd : diffAdds) {
            try {
                diffExecute(diffAdd, url);
                uploadOntologyToFuseki(new File(OUTPUT_FILE), dataset);
                uploadOntologyToFuseki(new File(QUAD_FILE), dataset);
            } catch (Exception e) {
                val invalidContoDiff = InvalidContoDiff.builder()
                        .sha(diffAdd.sha())
                        .parentSha(diffAdd.parentSha())
                        .message(e.getMessage())
                        .build();

                invalidContoDiffRepository.insert(invalidContoDiff);
                log.error(e.getMessage(), e);
            }
        }
    }

    public void updateByUrl(String url, Instant datetime, String dataset) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url, datetime);
        for (val diffAdd : diffAdds) {
            try {
                diffExecute(diffAdd, url);
                uploadOntologyToFuseki(new File(OUTPUT_FILE), dataset);
                uploadOntologyToFuseki(new File(QUAD_FILE), dataset);
            } catch (Exception e) {
                val invalidContoDiff = InvalidContoDiff.builder()
                        .sha(diffAdd.sha())
                        .parentSha(diffAdd.parentSha())
                        .message(e.getMessage())
                        .build();

                invalidContoDiffRepository.insert(invalidContoDiff);
                log.error(e.getMessage(), e);
            }
        }
    }

    private static String getCommand(DiffAdd diffAdd, String baseUri) {
       String gitInfo = "\"" +
                diffAdd.gitUrlLeft() + UNIQUE_DELIMITER +
                diffAdd.gitUrlRight() + UNIQUE_DELIMITER +
                diffAdd.gitCommitUrlLeft() + UNIQUE_DELIMITER +
                diffAdd.gitCommitUrlRight() + UNIQUE_DELIMITER +
                diffAdd.datetime() + UNIQUE_DELIMITER +
                diffAdd.parentDatetime() + UNIQUE_DELIMITER +
                diffAdd.messageLeft().replaceAll("\\s", "_") + UNIQUE_DELIMITER +
                diffAdd.messageRight().replaceAll("\\s", "_") + "\"";

        return "java -jar ContoDiff-1.0-SNAPSHOT-shaded.jar" +
                " -base-iri " + baseUri +
                " -git_info " + gitInfo +
                " -o " + OUTPUT_FILE;
    }

    private void diffExecute(DiffAdd diffAdd, String baseUri) throws Exception {

        String command = getCommand(diffAdd, baseUri);

            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            val error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            val errorString = error.readLine();
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = error.readLine()) != null) {
                errorOutput.append(line).append(System.lineSeparator());
            }
            if (errorString != null) {
                throw new IOException(errorString);
            }
    }

    private Model readOntology(String ont) {
        try {
            new URI(ont);
            return RDFDataMgr.loadModel(ont);
        } catch (URISyntaxException e) {
            Model model = ModelFactory.createDefaultModel();
            InputStream stream = new ByteArrayInputStream(ont.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(model, stream, Lang.TURTLE);
            return model;
        }
    }

    private Model readOntology(File ont) throws IOException {
        String fileName = ont.getName();
        Path filePath = ont.toPath();
        Lang lang = RDFLanguages.filenameToLang(fileName);

        Model model = ModelFactory.createDefaultModel();
        InputStream stream = new ByteArrayInputStream(Files.readAllBytes(filePath));
        RDFDataMgr.read(model, stream, lang);

        return model;
    }

    private Dataset readDataset(String fileName, Path filePath) throws IOException {
        Dataset dataset = TDBFactory.createDataset();
        RDFDataMgr.read(dataset, Files.newInputStream(filePath), RDFLanguages.filenameToLang(fileName));

        return dataset;
    }

    public void uploadOntologyToFuseki(File ont, String dataset) {
        fusekiAuthenticate();
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(datasetServiceUrl);
        if (RDFLanguages.filenameToLang(ont.getName()).equals(Lang.NQUADS)) {
            try {
                Dataset ds = readDataset(ont.getName(), ont.toPath());
                ds.listNames().forEachRemaining(name -> {
                    datasetAccessor.add(name, ds.getNamedModel(name));
                });
            } catch (IOException e) {
                log.error("Error reading dataset{}", e.getMessage(), e);
            }
        } else {
            try {
                Model ontologyModel = readOntology(ont);
                RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                        .destination(datasetServiceUrl);

                try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
                    conn.load(ontologyModel);
                }
            } catch (IOException e) {
                log.error("Error reading ontology{}", e.getMessage(), e);
            }
        }
    }

    public void uploadOntologyToFuseki(String ont, String dataset) {
        fusekiAuthenticate();
        Model ontologyModel = readOntology(ont);

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.load(ontologyModel);
        }
    }

    private static void fusekiAuthenticate() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        Credentials credentials = new UsernamePasswordCredentials("admin", "fuseki");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        HttpOp.setDefaultHttpClient(httpclient);
    }

}

