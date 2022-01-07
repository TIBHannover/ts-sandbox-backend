package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyReadService;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OntologyReadServiceImplTest {
    private OntologyReadService ontologyReadService;

    @BeforeEach
    void setUp() {
        ontologyReadService = new OntologyReadServiceImpl();
    }

    @Test
    void testReadOntology_shouldReturnOntModel() {
        OntModel ontModel = ontologyReadService.readOntology("http://purl.obolibrary.org/obo/rex.owl");
        assertNotNull(ontModel);
    }

    @Test
    void testReadOntology_shouldThrowException() {

        Assertions.assertThrows(org.apache.jena.atlas.web.HttpException.class, () ->
            ontologyReadService.readOntology("http://test.owl")
        );
    }

}
