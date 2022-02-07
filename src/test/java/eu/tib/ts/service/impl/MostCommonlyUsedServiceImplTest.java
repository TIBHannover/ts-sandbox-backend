package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.KeyValueResultDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.MostCommonlyUsedService;
import eu.tib.ts.service.OntologyFilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MostCommonlyUsedServiceImplTest extends TestData {
    @Mock
    private ProcessedOntologyRepository processedOntologyRepository;
    @Mock
    private OntologyFilterService filterService;

    private MostCommonlyUsedService mostCommonlyUsedService;

    @BeforeEach
    void setUp() {
        mostCommonlyUsedService = new MostCommonlyUsedServiceImpl(processedOntologyRepository, filterService);
    }

    @Test
    void testGetMostCommonlyUsedCharacteristics() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyRepository.findAll())
            .thenReturn(processedOntologies);
        when(filterService.filter(anyList(), any(Optional.class)))
            .thenReturn(processedOntologies);

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<KeyValueResultDto> page = mostCommonlyUsedService.getMostCommonlyUsedCharacteristics(
            Optional.empty(), CharacteristicsType.PROPERTY, Optional.empty(), pageRequest
        );

        assertNotNull(page);
        assertEquals(8, page.getContent().size());
    }
}
