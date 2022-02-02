package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.Config;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.OntologyReadService;
import eu.tib.ts.service.OntologyTraverseService;
import eu.tib.ts.service.PreProcessingService;
import eu.tib.ts.service.ProcessedOntologyService;
import lombok.SneakyThrows;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
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
    private OntologyReadService ontologyReadService;
    @Mock
    private OntologyTraverseService ontologyTraverseService;
    @Mock
    private ProcessedOntologyService processedOntologyService;

    @BeforeEach
    void setUp() {
        preProcessingService = new PreProcessingServiceImpl(
            tsRepository,
            ontologyReadService,
            ontologyTraverseService,
            processedOntologyService
        );
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_newOntologies_readOntologySuccessful() {
        when(processedOntologyService.findAll())
            .thenReturn(getProcessedOntologies());

        List<TsOntology> ontologies = getNewOntologies();
        when(tsRepository.getOntologies())
            .thenReturn(ontologies);

        when(ontologyReadService.readOntologyWithOwlApi(anyString()))
            .thenReturn(OWL_ONTOLOGY);

        when(ontologyReadService.readOntologyWithJenaApi(anyString()))
            .thenReturn(ONT_MODEL);

        preProcessingService.doPreProcessing();

        verify(ontologyTraverseService, times(ontologies.size()))
            .getImports(any(OWLOntology.class));

        verify(ontologyTraverseService, times(ontologies.size()))
            .getProperties(any(OntModel.class));

        verify(ontologyTraverseService, times(ontologies.size()))
            .getNamespaces(any(OWLOntology.class));

        verify(ontologyTraverseService, times(ontologies.size()))
            .getClasses(any(OntModel.class));
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_newOntologies_readOntologyUnsuccessful() {
        when(processedOntologyService.findAll())
            .thenReturn(getProcessedOntologies());

        List<TsOntology> ontologies = getNewOntologies();
        when(tsRepository.getOntologies())
            .thenReturn(ontologies);

        when(ontologyReadService.readOntologyWithOwlApi(anyString()))
            .thenReturn(null);

        when(ontologyReadService.readOntologyWithJenaApi(anyString()))
            .thenReturn(null);

        preProcessingService.doPreProcessing();

        verify(ontologyTraverseService, never())
            .getImports(any(OWLOntology.class));

        verify(ontologyTraverseService, never())
            .getProperties(any(OntModel.class));

        verify(ontologyTraverseService, never())
            .getNamespaces(any(OWLOntology.class));

        verify(ontologyTraverseService, never())
            .getClasses(any(OntModel.class));
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_oldOntologies_readOntologySuccessful() {
        when(processedOntologyService.findAll())
            .thenReturn(getProcessedOntologies());

        List<TsOntology> ontologies = getOldOntologies();
        when(tsRepository.getOntologies())
            .thenReturn(ontologies);

        lenient().when(ontologyReadService.readOntologyWithOwlApi(anyString()))
            .thenReturn(OWL_ONTOLOGY);

        lenient().when(ontologyReadService.readOntologyWithJenaApi(anyString()))
            .thenReturn(ONT_MODEL);

        preProcessingService.doPreProcessing();

        verify(ontologyTraverseService, never())
            .getImports(any(OWLOntology.class));

        verify(ontologyTraverseService, never())
            .getProperties(any(OntModel.class));

        verify(ontologyTraverseService, never())
            .getNamespaces(any(OWLOntology.class));

        verify(ontologyTraverseService, never())
            .getClasses(any(OntModel.class));
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
