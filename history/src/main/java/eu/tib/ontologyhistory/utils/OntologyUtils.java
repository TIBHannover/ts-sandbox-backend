package eu.tib.ontologyhistory.utils;

import eu.tib.ontologyhistory.model.AxiomsMarkdown;
import eu.tib.ontologyhistory.service.DualOntologySetProvider;
import lombok.extern.slf4j.Slf4j;
import org.geneontology.owl.differ.Differ;
import org.geneontology.owl.differ.render.BasicDiffRenderer;
import org.geneontology.owl.differ.render.MarkdownGroupedDiffRenderer;
import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Slf4j
public class OntologyUtils {

    private OntologyUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Path DIFF_PLAIN_OUTPUT = Path.of("diff-output-plain.txt");

    private static final Path DIFF_MARKDOWN_OUTPUT = Path.of("diff-output-markdown.md");

    public static OWLOntology loadOntology(File file) throws IOException {
        IOHelper ioHelper = new IOHelper();
        return ioHelper.loadOntology(file);
    }

    public static OWLOntology loadOntology(IRI iri) throws IOException {
        IOHelper ioHelper = new IOHelper();
        return ioHelper.loadOntology(iri);
    }

    public static DualOntologySetProvider getOwlOntologySetProvider(OWLOntology ontologyLeft, OWLOntology ontologyRight) {
        return new DualOntologySetProvider(ontologyLeft.getOWLOntologyManager(), ontologyRight.getOWLOntologyManager());
    }

    public static Optional<AxiomsMarkdown> getAxiomsMarkdown(OWLOntology ontologyLeft, OWLOntology ontologyRight, DualOntologySetProvider ontologySetProvider) {
        Differ.BasicDiff differ = Differ.diff(ontologyLeft, ontologyRight);
        Differ.GroupedDiff groupedForMarkdown = Differ.groupedDiff(differ);

        try {

            Files.write(DIFF_PLAIN_OUTPUT, BasicDiffRenderer.renderPlain(differ).getBytes());
            Files.write(DIFF_MARKDOWN_OUTPUT, MarkdownGroupedDiffRenderer.render(groupedForMarkdown, ontologySetProvider).getBytes());

            List<String> plainOutput = Files.readAllLines(DIFF_PLAIN_OUTPUT, StandardCharsets.UTF_8);
            String markdownOutput = Files.readString(DIFF_MARKDOWN_OUTPUT, StandardCharsets.UTF_8);
            return Optional.of(new AxiomsMarkdown(plainOutput, markdownOutput));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

}