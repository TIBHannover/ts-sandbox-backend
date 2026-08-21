package eu.tib.ontologyhistory.service.robot;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.UnloadableImportException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RobotDiffFailureClassifierTest {

    @Test
    void classifiesRateLimitedImportFailure() {
        Exception exception = new RuntimeException(importException(
                "http://www.w3.org/2004/02/skos/core#",
                new OWLOntologyCreationIOException(new IOException(
                        "Server returned HTTP response code: 429 for URL: https://www.w3.org/2004/02/skos/core"))));

        RobotDiffFailure failure = RobotDiffFailureClassifier.classify(exception);

        assertThat(failure.code()).isEqualTo(RobotDiffFailureCode.IMPORT_HTTP_ERROR);
        assertThat(failure.message()).contains("HTTP 429");
        assertThat(failure.message()).contains("http://www.w3.org/2004/02/skos/core#");
    }

    @Test
    void classifiesHtmlImportResponseAsImportParseFailure() {
        Exception exception = new RuntimeException(importException(
                "https://www.w3.org/2019/wot/hypermedia#",
                new org.semanticweb.owlapi.model.OWLOntologyCreationException(
                        "Problem parsing https://www.w3.org/2019/wot/hypermedia#",
                        new RuntimeException("Did not recognise RDF format object RDFa (mimeTypes=text/html)"))));

        RobotDiffFailure failure = RobotDiffFailureClassifier.classify(exception);

        assertThat(failure.code()).isEqualTo(RobotDiffFailureCode.IMPORT_PARSE_ERROR);
        assertThat(failure.message()).contains("non-OWL/RDF");
        assertThat(failure.message()).contains("https://www.w3.org/2019/wot/hypermedia#");
    }

    @Test
    void createsSpecificOutputTooLargeFailure() {
        RobotDiffFailure failure = RobotDiffFailureClassifier.outputTooLarge(15_000_000);

        assertThat(failure.code()).isEqualTo(RobotDiffFailureCode.OUTPUT_TOO_LARGE);
        assertThat(failure.message()).contains("MongoDB document limit");
    }

    @Test
    void classifiesMongoDocumentLimitFailureAsOutputTooLarge() {
        Exception exception = new RuntimeException("Payload document size is larger than maximum of 16777216.");

        RobotDiffFailure failure = RobotDiffFailureClassifier.classify(exception);

        assertThat(failure.code()).isEqualTo(RobotDiffFailureCode.OUTPUT_TOO_LARGE);
        assertThat(failure.message()).contains("MongoDB document limit");
    }

    private static UnloadableImportException importException(String importIri,
                                                            org.semanticweb.owlapi.model.OWLOntologyCreationException cause) {
        return new UnloadableImportException(cause,
                OWLManager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(importIri)));
    }
}
