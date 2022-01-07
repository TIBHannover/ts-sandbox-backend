package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.ProcessedOntologyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessedOntologyServiceTest {
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
}
