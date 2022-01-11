package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.ratio.RatioService;
import eu.tib.ts.service.ratio.impl.RatioByClassServiceImpl;
import eu.tib.ts.service.ratio.impl.RatioByPropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatioByClassServiceImplTest extends TestData {
    private RatioService ratioService;
    @Mock
    private ProcessedOntologyRepository processedOntologyRepository;

    @BeforeEach
    void setUp() {
        ratioService = new RatioByClassServiceImpl(processedOntologyRepository);
    }

    @Test
    void testGetRatio_shouldReturnRatio() {
        List<Ontology> ontologies = getOntologies().subList(0, 2);

        when(processedOntologyRepository.findByOntologyIdIn(anyList()))
            .thenReturn(getProcessedOntologies().subList(0, 2));

        double actual = ratioService.getRatio(ontologies);

        assertEquals(0.2, actual);
    }

    @Test
    void testGetRatio_shouldReturnZero() {
        List<Ontology> ontologies = getOntologies();

        when(processedOntologyRepository.findByOntologyIdIn(anyList()))
            .thenReturn(getProcessedOntologies());

        double actual = ratioService.getRatio(ontologies);

        assertEquals(0, actual);
    }
}
