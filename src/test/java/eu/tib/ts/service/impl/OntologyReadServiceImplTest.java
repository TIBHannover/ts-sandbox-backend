package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyReadService;
import lombok.SneakyThrows;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OntologyReadServiceImplTest {
    private OntologyReadService ontologyReadService;

    @BeforeEach
    void setUp() {
        ontologyReadService = new OntologyReadServiceImpl();
    }

    @Test
    void testReadOntologyWithOwlApi_shouldReturnOWLOntology() throws OWLOntologyCreationException {
        OWLOntology owlOntology = ontologyReadService.readOntologyWithOwlApi("http://purl.obolibrary.org/obo/rex.owl");
        assertNotNull(owlOntology);
    }

    @SneakyThrows
    @Test
    void testReadOntologyWithJenaApi_shouldReturnOntModel() {
        OntModel ontModel = ontologyReadService.readOntologyWithJenaApi("http://purl.obolibrary.org/obo/rex.owl");
        assertNotNull(ontModel);
    }

    @Test
    void testReadOntologyWithJenaApi_shouldThrowException() {

        Assertions.assertThrows(Exception.class, () ->
            ontologyReadService.readOntologyWithJenaApi("http://test.owl")
        );
    }

    @Test
    void testReadOntologyWithOwlApi_shouldThrowException() {

        Assertions.assertThrows(Exception.class, () ->
            ontologyReadService.readOntologyWithOwlApi("http://test.owl")
        );
    }

}
