package eu.tib.ontologyhistory.service;

import com.google.common.collect.Sets;
import eu.tib.ontologyhistory.dto.conto.GraphInfo;
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
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
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

    private static final Path ONTOLOGY_LEFT = Path.of("ontology-left.txt");

    private static final Path ONTOLOGY_RIGHT = Path.of("ontology-right.txt");

    private final RobotRepository robotRepository;

    private final GitDiffService gitDiffService;

    private final DiffMapper diffMapper;

    public List<DiffDto> findAll() {
        val diff = robotRepository.findAll();
        return diffMapper.entityToDto(diff);
    }

    public Set<GraphInfo> findAllUrls() {
        val diff = robotRepository.findAll();
        val urls = new HashSet<GraphInfo>();
        for (Diff d : diff) {
            urls.add(new GraphInfo(d.getUrl()));
        }
        return urls;
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

    public DiffDto findFirstByUrl(String url) {
        val diff = robotRepository.findFirstByUrl(url);
        return diffMapper.entityToDto(diff);
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

    public void create(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url);

        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, url);
        }

    }

    public void makeDiffFromGit(DiffAdd diffAdd, String url) {
        try {
            OWLOntology owlOntologyLeft = OntologyUtils.loadOntology(IRI.create(diffAdd.gitUrlLeft()));
            OWLOntology owlOntologyRight = OntologyUtils.loadOntology(IRI.create(diffAdd.gitUrlRight()));

            val ontologySetProvider = OntologyUtils.getOwlOntologySetProvider(owlOntologyLeft, owlOntologyRight);
            val axiomsMarkdown = OntologyUtils.getAxiomsMarkdown(owlOntologyLeft, owlOntologyRight, ontologySetProvider);
            val gitDiff = GitDiffService.makeDiff(Files.write(ONTOLOGY_LEFT, diffAdd.gitRawFileLeft().getBytes()), Files.write(ONTOLOGY_RIGHT, diffAdd.gitRawFileRight().getBytes()));

            if (axiomsMarkdown.isPresent()) {
                Map<String, List<Axiom>> axioms = ParserUtils.parseAxioms(axiomsMarkdown.get().plainOutput());
                Document markdown = new Document().append(MARKDOWN_DOCUMENT_KEY, axiomsMarkdown.get().markdownOutput());

                Diff.builder()
                        .url(url)
                        .sha(diffAdd.sha())
                        .parentSha(diffAdd.parentSha())
                        .datetime(diffAdd.parentDatetime())
                        .parentDatetime(diffAdd.parentDatetime())
                        .message(diffAdd.messageLeft())
                        .markdown(markdown)
                        .axioms(axioms)
                        .gitDiff(gitDiff)
                        .build();

            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

}
