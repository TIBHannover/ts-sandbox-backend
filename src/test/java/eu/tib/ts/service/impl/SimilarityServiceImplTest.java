package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.*;
import eu.tib.ts.repository.ProcessedOntologyRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimilarityServiceImplTest {
    private SimilarityService similarityService;
    @Mock
    private ProcessedOntologyRepository processedOntologyRepository;

    @BeforeEach
    void setUp() {
        similarityService = new SimilarityServiceImpl(processedOntologyRepository);
    }

    @Test
    void testGetSharedProperties() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(getProcessedOntologies());

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSharedPropertyUri(ontologies, pageRequest);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
    }

    @Test
    void testGetSharedProperties_noProcessedOntologies() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(Collections.emptyList());

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSharedPropertyUri(ontologies, pageRequest);

        assertNotNull(page);
        assertEquals(0, page.getContent().size());
    }

    @Test
    void testGetSharedPropertiesForExternalOntology() {
        when(processedOntologyRepository.findAll())
            .thenReturn(getProcessedOntologies());

        ExternalOntology externalOntology = ExternalOntology.builder()
            .ontologyId("ontology_100")
            .uri("https://something_100.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_200"))
            .build();

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSharedPropertyUri(externalOntology, pageRequest);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());

        assertTrue(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_0")));
        assertEquals(3,
            page.getContent().stream()
                .filter(similarity -> similarity.getName().equals("propertyUri_0"))
                .findFirst()
                .map(similarity -> similarity.getOntologies().size())
                .orElse(0)
        );

        assertTrue(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_1")));
        assertEquals(2,
            page.getContent().stream()
                .filter(similarity -> similarity.getName().equals("propertyUri_1"))
                .findFirst()
                .map(similarity -> similarity.getOntologies().size())
                .orElse(0)
        );

        assertFalse(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_20")));
        assertFalse(page.getContent().stream().anyMatch(similarity -> similarity.getName().equals("propertyUri_200")));
    }

    @Test
    void testGetSharedPropertiesForExternalOntology_noProcessedOntologies() {
        when(processedOntologyRepository.findAll())
            .thenReturn(Collections.emptyList());

        ExternalOntology externalOntology = ExternalOntology.builder()
            .ontologyId("ontology_100")
            .uri("https://something_100.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_200"))
            .build();

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSharedPropertyUri(externalOntology, pageRequest);

        assertNotNull(page);
        assertEquals(0, page.getContent().size());
    }

    @Test
    void testGetSharedClassUri() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(getProcessedOntologies());

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSharedClassUri(ontologies, pageRequest);

        assertNotNull(page);
        assertEquals(2, page.getContent().size());
    }

    @Test
    void testGetSharedClasses_noProcessedOntologies() {
        List<Ontology> ontologies = getOntologies();
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        when(processedOntologyRepository.findByOntologyIdIn(ids))
            .thenReturn(Collections.emptyList());

        PageRequest pageRequest = PageRequest.of(0, 100);
        Page<Similarity> page = similarityService.getSharedClassUri(ontologies, pageRequest);

        assertNotNull(page);
        assertEquals(0, page.getContent().size());
    }

    private List<Ontology> getOntologies() {
        Ontology ontology0 = TsOntology.builder()
            .ontologyId("ontology_0")
            .config(Config.builder().fileLocation("https://something0.ttl").build())
            .build();

        Ontology ontology1 = TsOntology.builder()
            .ontologyId("ontology_1")
            .config(Config.builder().fileLocation("https://something1.ttl").build())
            .build();

        Ontology ontology2 = TsOntology.builder()
            .ontologyId("ontology_2")
            .config(Config.builder().fileLocation("https://something2.ttl").build())
            .build();

        return List.of(ontology0, ontology1, ontology2);
    }

    private List<ProcessedOntology> getProcessedOntologies() {
        ProcessedOntology processedOntology0 = ProcessedOntology.builder()
            .id(0)
            .ontologyId("ontology_0")
            .uri("https://something0.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_20"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_20"))
            .build();

        ProcessedOntology processedOntology1 = ProcessedOntology.builder()
            .id(1)
            .ontologyId("ontology_1")
            .uri("https://something1.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_21"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_21"))
            .build();

        ProcessedOntology processedOntology2 = ProcessedOntology.builder()
            .id(2)
            .ontologyId("ontology_2")
            .uri("https://something2.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_2", "propertyUri_3"))
            .classes(Set.of("classUri_0", "classUri_2", "classUri_3"))
            .build();

        return List.of(processedOntology0, processedOntology1, processedOntology2);
    }
}
