package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.Config;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.PreProcessingOntologyService;
import eu.tib.ts.service.PreProcessingService;
import eu.tib.ts.service.ProcessedOntologyService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreProcessingServiceImplTest extends OntologyFileData {
    private PreProcessingService preProcessingService;
    @Mock
    private TsRepository tsRepository;
    @Mock
    private ProcessedOntologyService processedOntologyService;
    @Mock
    private PreProcessingOntologyService preProcessingOntologyService;

    @BeforeEach
    void setUp() {
        preProcessingService = new PreProcessingServiceImpl(
            tsRepository,
            processedOntologyService,
            preProcessingOntologyService,
            Collections.emptyList());
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_newOntologies_readOntologySuccessful() {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyService.findAll())
            .thenReturn(processedOntologies);

        List<TsOntology> ontologies = getNewOntologies();
        when(tsRepository.getOntologies())
            .thenReturn(ontologies);

        ProcessedOntology processedOntology = processedOntologies.get(0);
        when(preProcessingOntologyService.preProcess(any(Optional.class), anyString()))
            .thenReturn(processedOntology);

        preProcessingService.doPreProcessing();

        verify(processedOntologyService, times(2))
            .save(processedOntology);
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_oldOntologies_readOntologySuccessful() {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        when(processedOntologyService.findAll())
            .thenReturn(processedOntologies);

        List<TsOntology> ontologies = getOldOntologies();
        when(tsRepository.getOntologies())
            .thenReturn(ontologies);

        ProcessedOntology processedOntology = processedOntologies.get(0);

        preProcessingService.doPreProcessing();

        verify(preProcessingOntologyService, never())
            .preProcess(any(Optional.class), anyString());

        verify(processedOntologyService, never())
            .save(processedOntology);
    }

    private List<TsOntology> getNewOntologies() {
        TsOntology ontology0 = TsOntology.builder()
            .ontologyId("new_ontology_0")
            .config(Config.builder().fileLocation("https://something0.ttl").build())
            .build();

        TsOntology ontology1 = TsOntology.builder()
            .ontologyId("new_ontology_1")
            .config(Config.builder().fileLocation("https://something1.ttl").build())
            .build();

        return List.of(ontology0, ontology1);
    }

    private List<TsOntology> getOldOntologies() {
        TsOntology ontology0 = TsOntology.builder()
            .ontologyId("ontology_0")
            .config(Config.builder().fileLocation("https://something0.ttl").build())
            .build();

        TsOntology ontology1 = TsOntology.builder()
            .ontologyId("ontology_1")
            .config(Config.builder().fileLocation("https://something1.ttl").build())
            .build();

        return List.of(ontology0, ontology1);
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
