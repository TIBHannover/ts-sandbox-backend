package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.RatioDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.RatioService;
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
class RatioServiceImplTest extends TestData {
    private RatioService ratioService;
    @Mock
    private ProcessedOntologyRepository processedOntologyRepository;

    @BeforeEach
    void setUp() {
        ratioService = new RatioServiceImpl(processedOntologyRepository);
    }

    @Test
    void testGetRatio_shouldReturnRatio() {
        List<Ontology> ontologies = getOntologies().subList(0, 2);

        when(processedOntologyRepository.findByOntologyIdIn(anyList()))
            .thenReturn(getProcessedOntologies().subList(0, 2));

        RatioDto actual = ratioService.getRatio(ontologies, CharacteristicsType.CLASS);

        assertEquals(0.2, actual.getResult());
        assertEquals(1, actual.getSimilaritiesNumber());
        assertEquals(5, actual.getDistinctCharacteristicsNumber());
    }

    @Test
    void testGetRatio_shouldReturnZero() {
        List<Ontology> ontologies = getOntologies();

        when(processedOntologyRepository.findByOntologyIdIn(anyList()))
            .thenReturn(getProcessedOntologies());

        RatioDto actual = ratioService.getRatio(ontologies, CharacteristicsType.CLASS);

        assertEquals(0, actual.getResult());
        assertEquals(0, actual.getSimilaritiesNumber());
        assertEquals(8, actual.getDistinctCharacteristicsNumber());
    }
}
