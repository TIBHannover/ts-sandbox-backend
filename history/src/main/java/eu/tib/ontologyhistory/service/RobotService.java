package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.mapper.DiffMapper;
import eu.tib.ontologyhistory.model.Axiom;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.exception.RobotDiffExecutionException;
import eu.tib.ontologyhistory.repository.RobotRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.utils.ExceptionUtils;
import eu.tib.ontologyhistory.utils.OntologyUtils;
import eu.tib.ontologyhistory.utils.ParserUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.DiffCommand;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
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


@Slf4j
@AllArgsConstructor
@Service
public class RobotService {

    private static final String MARKDOWN_DOCUMENT_KEY = "file";

    private static final String DIFF_PLAIN_OUTPUT_FILE = "diff-plain.txt";

    private final RobotRepository robotRepository;

    private final DiffMapper diffMapper;

    public List<DiffDto> findAll() {
        val diff = robotRepository.findAll();
        return diffMapper.entityToDto(diff);
    }

    public Set<URI> findAllUrls() {
        val diff = robotRepository.findAll();
        val uris = new HashSet<URI>();
        for (Diff d : diff) {
            uris.add(d.getUri());
        }
        return uris;
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
        val diff = robotRepository.findFirstByParentSha(parentSha).orElse(null);
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

    public void deleteAll() {
        robotRepository.deleteAll();
    }

    public void update(String id) {
        // will be extended later
    }

    public void updateByUrl(URI uri, Instant datetime) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

        val diffAdds = gitService.getDiffAdds(uri, datetime);

        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, uri);
        }
    }

    public void create(URI uri) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

        val diffAdds = gitService.getDiffAdds(uri, null);

        diffAdds.forEach(diffAdd -> CompletableFuture.runAsync(() -> makeDiffFromGit(diffAdd, uri)));
    }

    public void create(URI uri, List<DiffAdd> diffAdds) {
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
            OWLOntology owlOntologyLeft = OntologyUtils.loadOntology(IRI.create(diffAdd.gitUrlLeft()));
            OWLOntology owlOntologyRight = OntologyUtils.loadOntology(IRI.create(diffAdd.gitUrlRight()));

            val ontologySetProvider = OntologyUtils.getOwlOntologySetProvider(owlOntologyLeft, owlOntologyRight);
            val axiomsMarkdown = OntologyUtils.getAxiomsMarkdown(owlOntologyLeft, owlOntologyRight, ontologySetProvider);

            if (axiomsMarkdown.isPresent()) {
                Map<String, List<Axiom>> axioms = ParserUtils.parseAxioms(axiomsMarkdown.get().plainOutput());
                Document markdown = new Document().append(MARKDOWN_DOCUMENT_KEY, axiomsMarkdown.get().markdownOutput());

                val diff = Diff.builder()
                        .uri(uri)
                        .sha(diffAdd.sha())
                        .parentSha(diffAdd.parentSha())
                        .datetime(diffAdd.parentDatetime())
                        .parentDatetime(diffAdd.parentDatetime())
                        .message(diffAdd.messageLeft())
                        .markdown(markdown)
                        .axioms(axioms)
                        .build();

                robotRepository.insert(diff);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Map<String, List<String>> resHistory(URI uri, Instant datetime, String resourceIRI) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

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
