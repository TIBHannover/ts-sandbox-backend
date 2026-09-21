package eu.tib.ontologyhistory.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import eu.tib.ontologyhistory.dto.conto.*;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.InvalidContoDiff;
import eu.tib.ontologyhistory.repository.InvalidContoDiffRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.utils.SparqlQueries;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ContoDiffMain;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.stereotype.Service;
import org.webdifftool.client.model.DiffContext;

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

    private static final String FUSEKI_DOCKER_CONN_STRING = "http://fuseki:3030/";

    private static final String OUTPUT_FILE = "output.ttl";

    private static final String QUAD_FILE = "all_diffs.nq";

    private static final String ONTOLOGY_LEFT = "ontology-left.txt";

    private static final String ONTOLOGY_RIGHT = "ontology-right.txt";

    private static final String STAGE_CONTO_EXECUTION = "CONTO_EXECUTION";

    private static final String STAGE_CONTO_PARSE_ERROR = "CONTO_PARSE_ERROR";

    private static final String STAGE_OUTPUT_VALIDATION = "OUTPUT_VALIDATION";

    private static final String STAGE_EMPTY_CHANGE_GRAPH = "EMPTY_CHANGE_GRAPH";

    private static final String STAGE_UNLINKED_CHANGE_GRAPH = "UNLINKED_CHANGE_GRAPH";

    private static final String STAGE_FUSEKI_UPLOAD = "FUSEKI_UPLOAD";

    private static final String STAGE_QUERY_VERIFICATION = "QUERY_VERIFICATION";

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

    public List<Timeline> findByUrl(URI uri, String dataset) {
        List<Timeline> timelines = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE);
                graphQuery.setParam("ontologyURI", ResourceFactory.createResource(String.valueOf(uri)));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode commitLabel;
                    RDFNode message;
                    RDFNode firstCommitTime;
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

    public List<String> findFirstByUrl(URI uri, String dataset) {
        fusekiAuthenticate();
        val res = new ArrayList<String>();
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.GET_GRAPH_BY_URL);
                graphQuery.setParam("ontologyURI", ResourceFactory.createResource(String.valueOf(uri)));
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

        val invalidDiff = invalidContoDiffRepository.findFirstByParentShaOrderByCreatedAtDesc(commitId);
        if (invalidDiff != null) {
            return new Difference(null, formatInvalidDiffMessage(invalidDiff));
        }

        fusekiAuthenticate();
        return new Difference(findTimelineChanges(commitId, dataset), null);
    }

    public Map<Instant, Collection<TimelineMessage>> timelineMessage(String dataset, URI uri, String resourceUri, String firstCommitTime, String secondCommitTime) {
        fusekiAuthenticate();
        Multimap<Instant, TimelineMessage> result = ArrayListMultimap.create();
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE_MESSAGE);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(String.valueOf(uri)));
                graphQuery.setParam("resourceArg", ResourceFactory.createResource(resourceUri));
                graphQuery.setLiteral("firstCommitTime", firstCommitTime);
                graphQuery.setLiteral("secondCommitTime", secondCommitTime);
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode ppLabel;
                    RDFNode commitTime;
                    RDFNode p;
                    RDFNode o;
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

        return result.asMap();
    }

    public List<String> operations(String dataset, URI uri) {
        List<String> functions = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.ALL_DISTINCT_OPERATIONS);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(String.valueOf(uri)));
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

    public List<String> operationsData(String dataset, URI uri) {
        List<String> subjects = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.DATA_RELATED_TO_OPERATION);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(String.valueOf(uri)));
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

    public List<Timeline> getVersions(String dataset, URI uri) {
        List<Timeline> subjects = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);

        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.WHOLE_ONTOLOGY_TIMELINE);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(String.valueOf(uri)));
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode message;
                    RDFNode versionTime;
                    RDFNode commitLabel;
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

    public List<String> dataInBetweenDates(String dataset, URI uri, String startDatetime, String endDatetime) {
        List<String> subjects = new ArrayList<>();
        fusekiAuthenticate();

        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + SparqlQueries.DATA_BETWEEN_TWO_DATES);
                graphQuery.setParam("ontologyURL", ResourceFactory.createResource(String.valueOf(uri)));
                graphQuery.setLiteral("startDatetimeArg", startDatetime);
                graphQuery.setLiteral("endDatetimeArg", endDatetime);
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    ResultSet results = qExec.execSelect();
                    RDFNode ppLabel;
                    RDFNode subject;
                    RDFNode object;
                    RDFNode predicate;
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
        // will be extended later
    }

    public void deleteAll() {
        // will be extended later
        invalidContoDiffRepository.deleteAll();
    }

    public void update(String id) {
        // will be extended later
    }

    private String apacheDatetimeToInstant(String datetime) {
        return datetime.split("\\^\\^")[0];
    }

    public void create(URI uri, String dataset) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val diffAdds = gitService.getDiffAdds(uri, null);
        invalidContoDiffRepository.deleteAllByUri(String.valueOf(uri));
        for (val diffAdd : diffAdds) {
            create(uri, diffAdd, dataset);
        }
    }

    public void create(URI uri, List<DiffAdd> diffAdds, String dataset) {
        invalidContoDiffRepository.deleteAllByUri(String.valueOf(uri));
        diffAdds.forEach(diffAdd -> create(uri, diffAdd, dataset));
    }

    public void createIncremental(URI uri, List<DiffAdd> diffAdds, String dataset) {
        diffAdds.forEach(diffAdd -> create(uri, diffAdd, dataset));
    }

    public boolean hasStoredOrInvalidDiff(URI uri, String parentSha, String dataset) {
        if (invalidContoDiffRepository.existsByUriAndParentSha(String.valueOf(uri), parentSha)) {
            return true;
        }
        try {
            return !findTimelineChanges(parentSha, dataset).isEmpty();
        } catch (Exception e) {
            log.warn("Could not check existing COnto data for ontology {} commit {}", uri, parentSha, e);
            return false;
        }
    }

    private void create(URI uri, DiffAdd diffAdd, String dataset) {
        invalidContoDiffRepository.deleteAllByUriAndParentSha(String.valueOf(uri), diffAdd.parentSha());
        ContoGeneratedFiles generatedFiles;
        try {
            generatedFiles = getCommand(diffAdd, uri);
        } catch (Exception e) {
            if (isContoParseFailure(e)) {
                recordInvalidDiff(uri, diffAdd, STAGE_CONTO_PARSE_ERROR,
                        "COnto could not parse one ontology version for this commit. The downloaded file may not be valid RDF/OWL for COnto.",
                        e, null, null);
            } else {
                recordInvalidDiff(uri, diffAdd, STAGE_CONTO_EXECUTION, "COnto execution failed before output files could be validated.", e, null, null);
            }
            return;
        }

        try {
            validateGeneratedFile(generatedFiles.outputFile(), OUTPUT_FILE);
            validateGeneratedChangeGraph(generatedFiles.quadFile());
        } catch (Exception e) {
            recordInvalidDiff(uri, diffAdd, STAGE_OUTPUT_VALIDATION, "COnto finished, but one or more generated output files were missing or empty.", e, generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
            return;
        }

        if (generatedFiles.quadSizeBytes() == 0) {
            recordInvalidDiff(uri, diffAdd, STAGE_EMPTY_CHANGE_GRAPH,
                    "COnto did not produce queryable change triples for this commit. Git diff and ROBOT may still show changes, but this edit was not represented by COnto's change model.",
                    null, generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
            return;
        }

        try {
            uploadOntologyToFuseki(generatedFiles.outputFile().toFile(), dataset);
            uploadOntologyToFuseki(generatedFiles.quadFile().toFile(), dataset);
        } catch (Exception e) {
            recordInvalidDiff(uri, diffAdd, STAGE_FUSEKI_UPLOAD, "COnto generated output, but the output could not be uploaded to Fuseki.", e, generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
            return;
        }

        try {
            val changes = findTimelineChanges(diffAdd.parentSha(), dataset);
            if (changes.isEmpty()) {
                if (hasTimelineOperationLinks(diffAdd.parentSha(), dataset)) {
                    recordInvalidDiff(uri, diffAdd, STAGE_QUERY_VERIFICATION,
                            "COnto generated and uploaded output, but no queryable changes were found for this commit. This usually means the generated RDF shape does not match the SPARQL query assumptions.",
                            null, generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
                } else {
                    recordInvalidDiff(uri, diffAdd, STAGE_UNLINKED_CHANGE_GRAPH,
                            "COnto generated N-Quads, but did not link any change operation to this commit. The output cannot be displayed reliably because the commit metadata and change graphs are disconnected.",
                            null, generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
                }
            } else {
                log.info("COnto generated {} queryable change(s) for ontology {} commit {}. output.ttl={} bytes, all_diffs.nq={} bytes",
                        changes.size(), uri, diffAdd.parentSha(), generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
            }
        } catch (Exception e) {
            recordInvalidDiff(uri, diffAdd, STAGE_QUERY_VERIFICATION, "COnto generated and uploaded output, but querying the stored changes failed.", e, generatedFiles.outputSizeBytes(), generatedFiles.quadSizeBytes());
        }
    }

    private static ContoGeneratedFiles getCommand(DiffAdd diffAdd, URI baseUrl) throws IOException {
        val runDirectory = Files.createTempDirectory("conto-diff-");
        val ontLeft = Files.writeString(runDirectory.resolve(ONTOLOGY_LEFT), diffAdd.gitRawFileLeft());
        val ontRight = Files.writeString(runDirectory.resolve(ONTOLOGY_RIGHT), diffAdd.gitRawFileRight());
        val outputFile = runDirectory.resolve(OUTPUT_FILE);
        val quadFile = runDirectory.resolve(QUAD_FILE);

        val diffContext = DiffContext.builder()
                .fileLeft(ontLeft.toString())
                .fileRight(ontRight.toString())
                .rawUrlLeft(diffAdd.gitUrlLeft())
                .rawUrlRight(diffAdd.gitUrlRight())
                .leftCommitUri(diffAdd.gitCommitUrlLeft())
                .rightCommitUri(diffAdd.gitCommitUrlRight())
                .leftDatetime(diffAdd.datetime().toString())
                .rightDatetime(diffAdd.parentDatetime().toString())
                .leftMessage(diffAdd.messageLeft().replaceAll("\\s", "_").replace("\"", "'"))
                .rightMessage(diffAdd.messageRight().replaceAll("\\s", "_").replace("\"", "'"))
                .outputFile(outputFile.toString())
                .allDiffsNQuadFile(quadFile.toString())
                .build();

        try {
            ContoDiffMain.makeContoDiff(diffContext, String.valueOf(baseUrl));
        } catch (OWLOntologyCreationException | IOException e) {
            throw new RuntimeException(e);
        }

        return new ContoGeneratedFiles(outputFile, quadFile, fileSize(outputFile), fileSize(quadFile));
    }

    private static long fileSize(Path file) {
        try {
            return Files.exists(file) ? Files.size(file) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    private void validateGeneratedFile(Path file, String label) {
        if (!Files.exists(file)) {
            throw new IllegalStateException(label + " was not created by COnto");
        }
        if (fileSize(file) == 0) {
            throw new IllegalStateException(label + " was created by COnto but is empty");
        }
    }

    private void validateGeneratedChangeGraph(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalStateException(QUAD_FILE + " was not created by COnto");
        }
    }

    private List<String> findTimelineChanges(String commitId, String dataset) {
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
                    RDFNode ppLabel;
                    RDFNode s;
                    RDFNode p;
                    RDFNode o;
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
        return result;
    }

    private boolean hasTimelineOperationLinks(String commitId, String dataset) {
        String datasetServiceUrl = FUSEKI_DOCKER_CONN_STRING + dataset;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination(datasetServiceUrl);
        val hasOperation = new boolean[]{false};
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Txn.executeRead(conn, () -> {
                ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
                graphQuery.setCommandText(SparqlQueries.ONDET_PREFIXES + """
                        ASK
                        WHERE {
                          ?commit_id rdfs:label ?githubCommit .
                          ?operation ?pp ?operation_commit .
                          FILTER(?pp != prov:atLocation &&
                                 ?pp != prov:wasAssociatedWith &&
                                 ?pp != prov:dm &&
                                 ?pp != rdf:type &&
                                 ?pp != rdfs:label) .
                          BIND(REPLACE(STR(?operation_commit), "^.*/", "") AS ?operation_commit_label) .
                          FILTER(?operation_commit = ?commit_id || ?operation_commit_label = STR(?githubCommit)) .
                          ?operation prov:atLocation ?location .
                          ?location prov:dm ?diff .

                          FILTER(str(?githubCommit) = ?commitId) .
                        }
                        """);
                graphQuery.setLiteral("commitId", commitId);
                try (QueryExecution qExec = QueryExecutionFactory.sparqlService(datasetServiceUrl, graphQuery.asQuery())) {
                    hasOperation[0] = qExec.execAsk();
                }
            });
        }
        return hasOperation[0];
    }

    private void recordInvalidDiff(URI uri, DiffAdd diffAdd, String stage, String userMessage, Exception exception,
                                   Long outputSizeBytes, Long quadSizeBytes) {
        val technicalDetail = exception == null ? "" : formatTechnicalDetail(exception);
        val message = userMessage + technicalDetail;
        val invalidContoDiff = InvalidContoDiff.builder()
                .uri(String.valueOf(uri))
                .sha(diffAdd.sha())
                .parentSha(diffAdd.parentSha())
                .stage(stage)
                .message(message)
                .outputSizeBytes(outputSizeBytes)
                .quadSizeBytes(quadSizeBytes)
                .createdAt(Instant.now())
                .build();

        invalidContoDiffRepository.insert(invalidContoDiff);
        if (exception == null) {
            log.warn("COnto diff issue for ontology {} commit {} at stage {}: {} output.ttl={} bytes all_diffs.nq={} bytes",
                    uri, diffAdd.parentSha(), stage, message, outputSizeBytes, quadSizeBytes);
        } else if (STAGE_CONTO_PARSE_ERROR.equals(stage)) {
            log.warn("COnto diff failed for ontology {} commit {} at stage {}: {} output.ttl={} bytes all_diffs.nq={} bytes",
                    uri, diffAdd.parentSha(), stage, message, outputSizeBytes, quadSizeBytes);
        } else {
            log.error("COnto diff failed for ontology {} commit {} at stage {}: {} output.ttl={} bytes all_diffs.nq={} bytes",
                    uri, diffAdd.parentSha(), stage, message, outputSizeBytes, quadSizeBytes, exception);
        }
    }

    private static boolean isContoParseFailure(Throwable throwable) {
        val text = collectExceptionText(throwable).toLowerCase(Locale.ROOT);
        return text.contains("unparsableontologyexception")
                || text.contains("could not parse ontology")
                || text.contains("problem parsing")
                || text.contains("parseexception")
                || text.contains("rdfparseexception");
    }

    private static String formatTechnicalDetail(Throwable throwable) {
        val rawText = collectExceptionText(throwable);
        if (rawText.isBlank()) {
            return "";
        }

        val parserDetail = findParserDetail(rawText);
        val detail = parserDetail.isBlank() ? rawText : parserDetail;
        return " Technical detail: " + truncate(cleanExceptionDetail(detail), 700);
    }

    private static String collectExceptionText(Throwable throwable) {
        StringBuilder text = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getClass().getSimpleName() != null && !current.getClass().getSimpleName().isBlank()) {
                text.append(current.getClass().getSimpleName());
            }
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                text.append(": ").append(current.getMessage());
            }
            text.append('\n');
            current = current.getCause();
        }
        return text.toString();
    }

    private static String findParserDetail(String rawText) {
        val priorities = List.of(
                "RDFParseException",
                "ParseException",
                "Expected ",
                "IRI included an unencoded space",
                "Content is not allowed in prolog",
                "Encountered ",
                "Lexical error",
                "Problem parsing"
        );

        for (val priority : priorities) {
            val fallbackCandidates = new ArrayList<String>();
            for (val line : rawText.split("\\R")) {
                val trimmed = line.trim();
                if (trimmed.contains(priority)) {
                    if (!trimmed.contains("ManchesterOWLSyntaxOntologyParser")) {
                        return trimmed;
                    }
                    fallbackCandidates.add(trimmed);
                }
            }
            if (!fallbackCandidates.isEmpty()) {
                return fallbackCandidates.get(0);
            }
        }

        for (val line : rawText.split("\\R")) {
            val trimmed = line.trim();
            if (!trimmed.isBlank()) {
                return trimmed;
            }
        }

        return "";
    }

    private static String cleanExceptionDetail(String detail) {
        return detail
                .replaceAll("file:/tmp/conto-diff-[^\\s)]*/", "")
                .replaceAll("/tmp/conto-diff-[^\\s)]*/", "")
                .replaceAll("\\s+[\\w.$]+\\([\\w.]+\\.java:\\d+\\).*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String formatInvalidDiffMessage(InvalidContoDiff invalidDiff) {
        val stage = invalidDiff.getStage() == null ? "UNKNOWN" : invalidDiff.getStage();
        val outputSize = invalidDiff.getOutputSizeBytes() == null ? "unknown" : invalidDiff.getOutputSizeBytes().toString();
        val quadSize = invalidDiff.getQuadSizeBytes() == null ? "unknown" : invalidDiff.getQuadSizeBytes().toString();
        return String.format("COnto diff is not available for this commit. Stage: %s. %s Output size: output.ttl=%s bytes, all_diffs.nq=%s bytes.",
                stage, invalidDiff.getMessage(), outputSize, quadSize);
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
        if (Lang.NQUADS.equals(RDFLanguages.filenameToLang(ont.getName()))) {
            try {
                Dataset ds = readDataset(ont.getName(), ont.toPath());
                ds.listNames().forEachRemaining(name -> datasetAccessor.add(name, ds.getNamedModel(name)));
            } catch (IOException e) {
                throw new IllegalStateException("Error reading generated COnto dataset " + ont.getName(), e);
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
                throw new IllegalStateException("Error reading generated COnto ontology " + ont.getName(), e);
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

    private record ContoGeneratedFiles(
            Path outputFile,
            Path quadFile,
            long outputSizeBytes,
            long quadSizeBytes
    ) {
    }

}
