package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.mapper.DiffMapper;
import eu.tib.ontologyhistory.model.Axiom;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.exception.RobotDiffExecutionException;
import eu.tib.ontologyhistory.repository.RobotRepository;
import eu.tib.ontologyhistory.service.robot.RobotDiffFailure;
import eu.tib.ontologyhistory.service.robot.RobotDiffFailureClassifier;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.utils.ExceptionUtils;
import eu.tib.ontologyhistory.utils.OntologyUtils;
import eu.tib.ontologyhistory.utils.ParserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.DiffCommand;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class RobotService {

    private static final String MARKDOWN_DOCUMENT_KEY = "file";

    private static final String DIFF_PLAIN_OUTPUT_FILE = "diff-plain.txt";

    private static final int MAX_MONGO_DOCUMENT_BYTES = 15_000_000;

    private static final String STATUS_AVAILABLE = "AVAILABLE";

    private static final String STATUS_FAILED = "FAILED";

    private static final String STATUS_SKIPPED = "SKIPPED";

    private final RobotRepository robotRepository;

    private final DiffMapper diffMapper;

    @Value("${ondet.robot.catalog.path:}")
    private String robotCatalogPath;

    private volatile boolean robotCatalogWarningLogged;

    public List<DiffDto> findAll() {
        val diff = robotRepository.findAll();
        return diffMapper.entityToDto(diff);
    }

    public Set<URI> findAllUrls() {
        return robotRepository.findAllUris()
                .stream()
                .map(item -> URI.create(item.getString("uri")))
                .collect(Collectors.toSet());
    }

    public DiffDto findById(String id) {
        val diff = robotRepository.findById(id).orElse(null);
        return diffMapper.entityToDto(diff);
    }

    public DiffDto findBySha(String sha) {
        val diff = robotRepository.findFirstBySha(sha).orElse(null);
        return diffMapper.entityToDto(diff);
    }

    public DiffDto findByParentSha(String parentSha) {
        val diff = robotRepository.findFirstByParentSha(parentSha)
                .or(() -> robotRepository.findFirstBySha(parentSha))
                .orElse(null);
        return diffMapper.entityToDto(diff);
    }

    public List<DiffDto> findAllByUrl(URI uri) {
        val diffs = robotRepository.findAllByUri(uri);
        return diffMapper.entityToDto(diffs);
    }

    public DiffDto findFirstByUrl(URI uri) {
        val diff = robotRepository.findFirstByUri(uri);
        return diffMapper.entityToDto(diff);
    }

    public void deleteById(String id) {
        robotRepository.deleteById(id);
    }

    public void deleteAllByUrl(URI uri) {
        robotRepository.deleteAllByUri(uri);
    }

    public boolean existsByUriAndParentSha(URI uri, String parentSha) {
        return robotRepository.existsByUriAndParentSha(uri, parentSha);
    }

    public void deleteAll() {
        robotRepository.deleteAll();
    }

    public void create(URI uri, List<DiffAdd> diffAdds) {
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, uri);
        }
    }

    public void createAsync(URI uri) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val diffAdds = gitService.getDiffAdds(uri, null);

        diffAdds.forEach(diffAdd -> CompletableFuture.runAsync(() -> makeDiffFromGit(diffAdd, uri)));
    }

    public void createAsync(URI uri, List<DiffAdd> diffAdds) {
        diffAdds.forEach(diffAdd -> CompletableFuture.runAsync(() -> makeDiffFromGit(diffAdd, uri)));
    }

    private void diffExecute(DiffAdd diffAdd, File output) {
        val diffCommand = new DiffCommand();
        try {
            diffCommand.execute(new CommandState(), new String[]
                    {
                            "--left-iri", diffAdd.gitUrlLeft(),
                            "--right-iri", diffAdd.gitUrlRight(),
                            "--output", output.getName(),
                            "--format", "markdown"
                    });
        } catch (Exception e) {
            val throwable = ExceptionUtils.findRootCause(e);
            if (!ExceptionUtils.handleCustomException(throwable)) {
                throw new RobotDiffExecutionException("Some general error happened during diff execution");
            }
        }

    }

    public void makeDiffFromGit(DiffAdd diffAdd, URI uri) {
        try {
            val ontLeft = Files.createTempFile("left-file", ".txt");
            val ontRight = Files.createTempFile("right-file", ".txt");
            File catalogFile = robotCatalogFile().orElse(null);
            OWLOntology owlOntologyLeft = OntologyUtils.loadOntology(Files.write(ontLeft, diffAdd.gitRawFileLeft().getBytes()).toFile(), catalogFile);
            OWLOntology owlOntologyRight = OntologyUtils.loadOntology(Files.write(ontRight, diffAdd.gitRawFileRight().getBytes()).toFile(), catalogFile);

            val ontologySetProvider = OntologyUtils.getOwlOntologySetProvider(owlOntologyLeft, owlOntologyRight);
            val axiomsMarkdown = OntologyUtils.getAxiomsMarkdown(owlOntologyLeft, owlOntologyRight, ontologySetProvider);

            if (axiomsMarkdown.isPresent()) {
                Map<String, List<Axiom>> axioms = ParserUtils.parseAxioms(axiomsMarkdown.get().plainOutput());
                Document markdown = new Document().append(MARKDOWN_DOCUMENT_KEY, axiomsMarkdown.get().markdownOutput());

                if (markdown.toJson().getBytes().length > MAX_MONGO_DOCUMENT_BYTES) {
                    val failure = RobotDiffFailureClassifier.outputTooLarge(MAX_MONGO_DOCUMENT_BYTES);
                    log.warn("Skipping ROBOT diff for ontology {} commit {}: {}", uri, diffAdd.parentSha(), failure.message());
                    insertRobotFailure(uri, diffAdd, STATUS_SKIPPED, failure);
                    return;
                }

                val diff = Diff.builder()
                        .uri(uri)
                        .sha(diffAdd.sha())
                        .parentSha(diffAdd.parentSha())
                        .datetime(diffAdd.datetime())
                        .parentDatetime(diffAdd.parentDatetime())
                        .message(diffAdd.messageRight())
                        .markdown(markdown)
                        .axioms(axioms)
                        .processingStatus(STATUS_AVAILABLE)
                        .build();

                robotRepository.insert(diff);
            } else {
                insertRobotFailure(uri, diffAdd, STATUS_FAILED, RobotDiffFailureClassifier.outputMissing());
            }
            ontLeft.toFile().delete();
            ontRight.toFile().delete();
        } catch (Exception e) {
            val failure = describeRobotFailure(e);
            log.error("ROBOT diff failed for ontology {} commit {}: {}", uri, diffAdd.parentSha(), failure.message(), e);
            insertRobotFailure(uri, diffAdd, STATUS_FAILED, failure);
        }
    }

    private void insertRobotFailure(URI uri, DiffAdd diffAdd, String processingStatus, RobotDiffFailure failure) {
        val diff = Diff.builder()
                .uri(uri)
                .sha(diffAdd.sha())
                .parentSha(diffAdd.parentSha())
                .datetime(diffAdd.datetime())
                .parentDatetime(diffAdd.parentDatetime())
                .message(diffAdd.messageRight())
                .markdown(new Document())
                .axioms(Collections.emptyMap())
                .processingStatus(processingStatus)
                .error(failure.message())
                .errorCode(failure.code().name())
                .build();

        robotRepository.insert(diff);
    }

    private RobotDiffFailure describeRobotFailure(Exception exception) {
        return RobotDiffFailureClassifier.classify(exception);
    }

    private Optional<File> robotCatalogFile() {
        if (robotCatalogPath == null || robotCatalogPath.isBlank()) {
            return Optional.empty();
        }

        File catalogFile = Path.of(robotCatalogPath).toFile();
        if (catalogFile.isFile()) {
            return Optional.of(catalogFile);
        }

        if (!robotCatalogWarningLogged) {
            log.warn("ROBOT catalog path is configured but does not point to a readable file: {}", robotCatalogPath);
            robotCatalogWarningLogged = true;
        }
        return Optional.empty();
    }

    public Map<String, List<String>> resHistory(URI uri, Instant datetime, String resourceIRI) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val diffAdds = gitService.getDiffAdds(uri, datetime);

        val objects = new LinkedHashMap<String, List<String>>();
        for (val diffAdd : diffAdds) {
            try {
                diffExecute(diffAdd, new File(DIFF_PLAIN_OUTPUT_FILE));
                val result = Files.readAllLines(Path.of(DIFF_PLAIN_OUTPUT_FILE));

                val filteredResult = result.stream()
                        .filter(r -> r.startsWith("+") && containsResourceIRI(r, resourceIRI))
                        .map(r -> r.substring(2))
                        .toList();

                objects.put(diffAdd.sha(), filteredResult);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }

        }

        return objects;
    }

    private static boolean containsResourceIRI(String str, String resourceIRI) {
        Pattern pattern = Pattern.compile(Pattern.quote(resourceIRI));
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
