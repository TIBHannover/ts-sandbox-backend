package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.*;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.service.SimilarityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarityServiceImplTest extends TestData {
    private SimilarityService similarityService;
    @Mock
    private ProcessedOntologyRepository processedOntologyRepository;
    @Mock
    private OntologyFilterService filterService;

    @BeforeEach
    void setUp() {
        similarityService = new SimilarityServiceImpl(processedOntologyRepository, filterService);
    }

    @Test
    void testGetSimilarities() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(processedOntologies);
        when(filterService.filter(anyList(), any(Optional.class)))
            .thenReturn(processedOntologies);

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSimilarities(
            ids, CharacteristicsType.PROPERTY, Optional.empty(), pageRequest
        );

        assertNotNull(page);
        assertEquals(1, page.getContent().size());
    }

    @Test
    void testGetSimilarities_noProcessedOntologies() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(Collections.emptyList());

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSimilarities(
            ids, CharacteristicsType.PROPERTY, Optional.empty(), pageRequest
        );

        assertNotNull(page);
        assertEquals(0, page.getContent().size());
    }

    @Test
    void testGetSimilaritiesForExternalOntology() {
        when(processedOntologyRepository.findAll())
            .thenReturn(getProcessedOntologies());

        ExternalOntology externalOntology = ExternalOntology.builder()
            .ontologyId("ontology_100")
            .uri("https://something_100.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_200"))
            .build();

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSimilarities(
            externalOntology, CharacteristicsType.PROPERTY, Optional.empty(), pageRequest
        );

        assertNotNull(page);
        assertEquals(3, page.getContent().size());

        assertTrue(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_0")));
        assertEquals(2,
            page.getContent().stream()
                .filter(similarity -> similarity.getName().equals("propertyUri_0"))
                .findFirst()
                .map(similarity -> similarity.getOntologies().size())
                .orElse(0)
        );

        assertTrue(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_1")));
        assertEquals(1,
            page.getContent().stream()
                .filter(similarity -> similarity.getName().equals("propertyUri_1"))
                .findFirst()
                .map(similarity -> similarity.getOntologies().size())
                .orElse(0)
        );

        assertFalse(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_20")));
        assertFalse(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_300")));
    }

    @Test
    void testGetSimilaritiesForExternalOntology_noProcessedOntologies() {
        when(processedOntologyRepository.findAll())
            .thenReturn(Collections.emptyList());

        ExternalOntology externalOntology = ExternalOntology.builder()
            .ontologyId("ontology_100")
            .uri("https://something_100.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_200"))
            .build();

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSimilarities(
            externalOntology, CharacteristicsType.PROPERTY, Optional.empty(), pageRequest
        );

        assertNotNull(page);
        assertEquals(0, page.getContent().size());
    }
}
