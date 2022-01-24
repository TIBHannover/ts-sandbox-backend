package eu.tib.ts.model;

import eu.tib.ts.model.ontology.SimpleOntology;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleOntologyTest {
    @Test
    void test() {
        SimpleOntology ontology = SimpleOntology.builder()
            .ontologyId("ont1")
            .uri("uri")
            .build();

        assertEquals("ont1", ontology.getOntologyId());
        assertEquals("uri", ontology.getUri());
    }
}
