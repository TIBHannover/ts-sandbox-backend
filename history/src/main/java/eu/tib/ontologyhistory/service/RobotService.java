package eu.tib.ontologyhistory.service;

import com.google.common.collect.Sets;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.mapper.DiffMapper;
import eu.tib.ontologyhistory.model.Axiom;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.exception.RobotDiffExecutionException;
import eu.tib.ontologyhistory.repository.RobotRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.utils.ExceptionUtils;
import eu.tib.ontologyhistory.utils.FileUtils;
import eu.tib.ontologyhistory.utils.OntologyUtils;
import eu.tib.ontologyhistory.utils.ParserUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.geneontology.owl.differ.Differ;
import org.geneontology.owl.differ.render.BasicDiffRenderer;
import org.geneontology.owl.differ.render.MarkdownGroupedDiffRenderer;
import org.obolibrary.robot.CommandState;
import org.obolibrary.robot.DiffCommand;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


@Slf4j
@AllArgsConstructor
@Service
public class RobotService {

    private static final String MARKDOWN_DOCUMENT_KEY = "file";

    private final RobotRepository robotRepository;

    private final GitDiffService gitDiffService;

    private final DiffMapper diffMapper;

    public List<DiffDto> findAll() {
        val diff = robotRepository.findAll();
        return diffMapper.entityToDto(diff);
    }

    public DiffDto findById(String id) {
        val diff = robotRepository.findById(id).orElse(null);
        return diffMapper.entityToDto(diff);
    }

    public DiffDto findBySha(String sha) {
        val diff = robotRepository.findFirstBySha(sha).orElse(null);
        return diffMapper.entityToDto(diff);
    }

    public List<DiffDto> findAllByUrl(String url) {
        val diffs = robotRepository.findAllByUrl(url);
        return diffMapper.entityToDto(diffs);
    }

    public void deleteById(String id) {
        robotRepository.deleteById(id);
    }

    public void deleteAllByUrl(String url) {
        robotRepository.deleteAllByUrl(url);
    }

    public void deleteAll() {
        robotRepository.deleteAll();
    }

    public void update(String id) {

    }

    private static class DualOntologySetProvider implements OWLOntologySetProvider {

        @Serial
        private static final long serialVersionUID = -8942374248162307075L;
        private final Set<OWLOntology> ontologies = Sets.newIdentityHashSet();

        /**
         * Init a new DualOntologySetProvider for a left and right ontology.
         *
         * @param left OWLOntologySetProvider for left ontology
         * @param right OWLOntologySetProvider for right ontology
         */
        public DualOntologySetProvider(OWLOntologySetProvider left, OWLOntologySetProvider right) {
            ontologies.addAll(left.getOntologies());
            ontologies.addAll(right.getOntologies());
        }

        /**
         * Get the ontologies in the provider.
         *
         * @return Set of OWLOntologies
         */
        @Nonnull
        @Override
        public Set<OWLOntology> getOntologies() {
            return Collections.unmodifiableSet(ontologies);
        }
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

    public void create(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url);

        for (val diffAdd : diffAdds) {
            try {
                makeDiffFromGit(diffAdd, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public DiffDto makeDiffFromGit(DiffAdd diffAdd, String url) {
        String ontologyLeftFilename = "ontology-left";
        String ontologyRightFilename = "ontology-right";

        Path diffOutputPlainFile = Path.of("diff-output-plain.txt");
        Path diffOutputPlainMarkdown = Path.of("diff-output-markdown.md");

        try {
            File ontLeft = FileUtils.createTempFile(ontologyLeftFilename, diffAdd.gitRawFileLeft());
            File ontRight = FileUtils.createTempFile(ontologyRightFilename, diffAdd.gitRawFileRight());

            OWLOntology loadedOntologyLeft = OntologyUtils.loadOntology(ontLeft);
            OWLOntology loadedOntologyRight = OntologyUtils.loadOntology(ontRight);

            OWLOntologySetProvider ontologySetProvider = new DualOntologySetProvider(
                    loadedOntologyLeft.getOWLOntologyManager(),
                    loadedOntologyRight.getOWLOntologyManager()
            );

            Differ.BasicDiff differ = Differ.diff(loadedOntologyLeft, loadedOntologyRight);
            Differ.GroupedDiff groupedForMarkdown = Differ.groupedDiff(differ);

            Files.write(diffOutputPlainFile, BasicDiffRenderer.renderPlain(differ).getBytes());
            Files.write(diffOutputPlainMarkdown, MarkdownGroupedDiffRenderer.render(groupedForMarkdown, ontologySetProvider).getBytes());

            List<String> lines = Files.readAllLines(diffOutputPlainFile, StandardCharsets.UTF_8);
            String line = Files.readString(diffOutputPlainMarkdown, StandardCharsets.UTF_8);

            Map<String, List<Axiom>> axioms = ParserUtils.parseAxioms(lines);

            Document markdown = new Document().append(MARKDOWN_DOCUMENT_KEY, line);

            val diff = Diff.builder()
                    .url(url)
                    .sha(diffAdd.sha())
                    .parentSha(diffAdd.parentSha())
                    .datetime(diffAdd.parentDatetime())
                    .parentDatetime(diffAdd.parentDatetime())
                    .message(diffAdd.messageLeft())
                    .markdown(markdown)
                    .axioms(axioms)
                    .gitDiff(gitDiffService.makeDiff(ontLeft.toPath(), ontRight.toPath()))
                    .build();

            if (diff != null) {
                val savedDiff = robotRepository.insert(diff);
                return diffMapper.entityToDto(savedDiff);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error happened during diff creation: " + e.getMessage());
        }
        return null;
    }

}
