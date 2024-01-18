package eu.tib.ontologyhistory.service;

import com.google.common.collect.Sets;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.mapper.DiffMapper;
import eu.tib.ontologyhistory.model.Axiom;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.repository.DiffRepository;
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
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;


@Slf4j
@AllArgsConstructor
@Service
public class DiffService {

    private final DiffRepository diffRepository;

    private final DiffMapper diffMapper;

    public List<DiffDto> findAll() {
        val diff = diffRepository.findAll();
        return diffMapper.entityToDto(diff);
    }

    public DiffDto findById(String id) {
        val diff = diffRepository.findById(id).orElse(null);
        return diffMapper.entityToDto(diff);
    }

    public void deleteById(String id) {
        diffRepository.deleteById(id);
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


    public Diff makeDiffFromGit(DiffAdd diffAdd) {
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

            Document markdown = new Document().append("file", line);

            Diff diff = Diff.builder()
                    .ontologyId("default")
                    .markdown(markdown)
                    .timestamp(diffAdd.commitDate())
                    .sha(diffAdd.sha())
                    .parentSha(diffAdd.parentSha())
                    .parentOffsetDateTime(diffAdd.parentOffsetDateTime())
                    .shaOffsetDateTime(diffAdd.shaOffsetDateTime())
                    .axioms(axioms)
                    .message(diffAdd.message())
                    .build();

            if (diff != null) {
                return diffRepository.insert(diff);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error happened during diff creation: " + e.getMessage());
        }

        return null;
    }

    public void assignOntologyId(List<Diff> diffIds, String ontologyId) {
        for (Diff d : diffIds) {
            Diff diff = diffRepository.findById(d.getId()).orElse(null);
            if (diff != null) {
                diff.setOntologyId(ontologyId);
                diffRepository.save(diff);
            }
        }
    }

}
