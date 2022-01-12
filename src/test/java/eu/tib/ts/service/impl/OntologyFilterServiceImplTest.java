package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.service.OntologyFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OntologyFilterServiceImplTest extends TestData {
    private OntologyFilterService ontologyFilterService;

    @BeforeEach
    void setUp() {
        ontologyFilterService = new OntologyFilterServiceImpl();
    }

    @Test
    void testFilter_shouldFilterOntologies() {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        List<ProcessedOntology> actual = ontologyFilterService.filter(processedOntologies, Optional.of("collection1"));

        assertTrue(processedOntologies.size() > actual.size());
    }

    @Test
    void testFilter_shouldNotFilterOntologies() {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        List<ProcessedOntology> actual = ontologyFilterService.filter(processedOntologies, Optional.empty());

        assertEquals(processedOntologies.size(), actual.size());
    }
}
