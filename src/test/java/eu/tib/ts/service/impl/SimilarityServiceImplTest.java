package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.CharacteristicsInfo;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ExternalOntology;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        SimilaritySettings similaritySettings = SimilaritySettings.builder()
            .weight(Map.of(
                "property", 0.25,
                "class", 0.25,
                "import", 0.25,
                "namespace", 0.25
            ))
            .build();
        similarityService = new SimilarityServiceImpl(processedOntologyRepository, filterService, similaritySettings);
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
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyRepository.findAll())
            .thenReturn(processedOntologies);
        when(filterService.filter(anyList(), any(Optional.class)))
            .thenReturn(processedOntologies);

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

    @Test
    void testGetPairwiseSimilarityForInternalOntology() {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyRepository.findAll())
            .thenReturn(processedOntologies);
        when(filterService.filter(anyList(), any(Optional.class)))
            .thenReturn(processedOntologies);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PairwiseSimilarity> page =
            similarityService.getPairwiseSimilarity(Optional.empty(), Optional.empty(), pageRequest);

        assertNotNull(page);

        List<PairwiseSimilarity> content = page.getContent();
        assertEquals(1, content.size());
        assertEquals("ontology_0", content.get(0).getPair().getFirst());
        assertEquals("ontology_1", content.get(0).getPair().getSecond());
        assertEquals(1.0, content.get(0).getSum());

        Map<String, CharacteristicsInfo> characteristics = content.get(0).getCharacteristics();
        assertTrue(characteristics.containsKey(CharacteristicsType.PROPERTY.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.CLASS.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.IMPORT.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.NAMESPACE.name().toLowerCase()));
    }

    @Test
    void testGetPairwiseSimilarityForInternalOntology_withIds() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(processedOntologies);
        when(filterService.filter(anyList(), any(Optional.class)))
            .thenReturn(processedOntologies);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PairwiseSimilarity> page =
            similarityService.getPairwiseSimilarity(Optional.of(ids), Optional.empty(), pageRequest);

        assertNotNull(page);

        List<PairwiseSimilarity> content = page.getContent();
        assertEquals(1, content.size());
        assertEquals("ontology_0", content.get(0).getPair().getFirst());
        assertEquals("ontology_1", content.get(0).getPair().getSecond());
        assertEquals(1.0, content.get(0).getSum());

        Map<String, CharacteristicsInfo> characteristics = content.get(0).getCharacteristics();
        assertTrue(characteristics.containsKey(CharacteristicsType.PROPERTY.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.CLASS.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.IMPORT.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.NAMESPACE.name().toLowerCase()));
    }

    @Test
    void testGetPairwiseSimilaritiesForInternalOntology_noProcessedOntologies() {
        when(processedOntologyRepository.findAll())
            .thenReturn(Collections.emptyList());

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PairwiseSimilarity> page =
            similarityService.getPairwiseSimilarity(Optional.empty(), Optional.empty(), pageRequest);

        assertNotNull(page);
    }

    @Test
    void testGetPairwiseSimilarityForExternalOntology() {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyRepository.findAll())
            .thenReturn(processedOntologies);
        when(filterService.filter(anyList(), any(Optional.class)))
            .thenReturn(processedOntologies);

        ExternalOntology externalOntology = ExternalOntology.builder()
            .ontologyId("ontology_100")
            .uri("https://something_100.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_200"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_200"))
            .imports(Set.of("importUri_0", "importUri_1", "importUri_200"))
            .namespaces(Set.of("namespaceUri_0", "namespaceUri_1", "namespaceUri_200"))
            .build();

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PairwiseSimilarity> page =
            similarityService.getPairwiseSimilarity(externalOntology, Optional.empty(), pageRequest);

        assertNotNull(page);
        List<PairwiseSimilarity> content = page.getContent();
        assertEquals("ontology_0", content.get(0).getPair().getFirst());
        assertEquals("ontology_100", content.get(0).getPair().getSecond());
        assertEquals(2.0, content.get(0).getSum());

        Map<String, CharacteristicsInfo> characteristics = content.get(0).getCharacteristics();
        assertTrue(characteristics.containsKey(CharacteristicsType.PROPERTY.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.CLASS.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.IMPORT.name().toLowerCase()));
        assertTrue(characteristics.containsKey(CharacteristicsType.NAMESPACE.name().toLowerCase()));
    }

    @Test
    void testGetPairwiseSimilaritiesForExternalOntology_noProcessedOntologies() {
        when(processedOntologyRepository.findAll())
            .thenReturn(Collections.emptyList());

        ExternalOntology externalOntology = ExternalOntology.builder()
            .ontologyId("ontology_100")
            .uri("https://something_100.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_200"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_200"))
            .imports(Set.of("importUri_0", "importUri_1", "importUri_200"))
            .namespaces(Set.of("namespaceUri_0", "namespaceUri_1", "namespaceUri_200"))
            .build();

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PairwiseSimilarity> page =
            similarityService.getPairwiseSimilarity(externalOntology, Optional.empty(), pageRequest);

        assertNotNull(page);
    }
}
