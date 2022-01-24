package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.ProcessedOntologyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedOntologyServiceTest extends TestData {
    @Mock
    private ProcessedOntologyRepository repository;
    private ProcessedOntologyService processedOntologyService;

    @BeforeEach
    void setUp() {
        processedOntologyService = new ProcessedOntologyServiceImpl(repository);
    }

    @Test
    void testFindAll() {
        processedOntologyService.findAll();

        verify(repository, times(1))
            .findAll();
    }

    @Test
    void testSave() {
        ProcessedOntology processedOntology = ProcessedOntology.builder()
            .ontologyId("someId")
            .uri("https://something_100.ttl")
            .build();
        processedOntologyService.save(processedOntology);

        verify(repository, times(1))
            .save(processedOntology);
    }

    @Test
    void testGetOntologies() {
        Iterable<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(repository.findAll())
            .thenReturn(processedOntologies);

        List<OntologyDto> actual = processedOntologyService.getOntologies();

        assertEquals(3, actual.size());
        assertEquals("ontology_0", actual.get(0).getOntologyId());
        assertEquals("ontology_1", actual.get(1).getOntologyId());
        assertEquals("ontology_2", actual.get(2).getOntologyId());
    }

    @Test
    void testGetOntologyIds() {
        Iterable<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(repository.findAll())
            .thenReturn(processedOntologies);

        List<String> actual = processedOntologyService.getOntologyIds();

        assertEquals(3, actual.size());
        assertEquals("ontology_0", actual.get(0));
        assertEquals("ontology_1", actual.get(1));
        assertEquals("ontology_2", actual.get(2));
    }
}
