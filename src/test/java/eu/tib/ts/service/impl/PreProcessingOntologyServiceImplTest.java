package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.Config;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.service.OntologyReadService;
import eu.tib.ts.service.OntologyTraverseService;
import eu.tib.ts.service.PreProcessingOntologyService;
import lombok.SneakyThrows;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreProcessingOntologyServiceImplTest extends OntologyFileData {
    private PreProcessingOntologyService preProcessingOntologyService;
    @Mock
    private OntologyReadService ontologyReadService;
    @Mock
    private OntologyTraverseService ontologyTraverseService;

    @BeforeEach
    void setUp() {
        preProcessingOntologyService = new PreProcessingOntologyServiceImpl(
            ontologyReadService,
            ontologyTraverseService
        );
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_readOntologySuccessful() {
        TsOntology ontology0 = TsOntology.builder()
            .ontologyId("new_ontology_0")
            .config(Config.builder().fileLocation("https://something0.ttl").build())
            .build();

        when(ontologyReadService.readOntologyWithOwlApi(anyString()))
            .thenReturn(OWL_ONTOLOGY);

        when(ontologyReadService.readOntologyWithJenaApi(anyString()))
            .thenReturn(ONT_MODEL);

        ProcessedOntology ontology = preProcessingOntologyService.preProcess(Optional.of(ontology0), ontology0.getUri());

        assertNotNull(ontology);
    }

    @SneakyThrows
    @Test
    void testDoPreProcessing_readOntologyUnsuccessful() {
        TsOntology ontology0 = TsOntology.builder()
            .ontologyId("new_ontology_0")
            .config(Config.builder().fileLocation("https://something0.ttl").build())
            .build();

        when(ontologyReadService.readOntologyWithOwlApi(anyString()))
            .thenReturn(null);

        when(ontologyReadService.readOntologyWithJenaApi(anyString()))
            .thenReturn(null);

        ProcessedOntology ontology = preProcessingOntologyService.preProcess(Optional.of(ontology0), ontology0.getUri());

        assertNotNull(ontology);

        verify(ontologyTraverseService, never())
            .getImports(any(OWLOntology.class));

        verify(ontologyTraverseService, never())
            .getProperties(any(OntModel.class));

        verify(ontologyTraverseService, never())
            .getNamespaces(any(OWLOntology.class));

        verify(ontologyTraverseService, never())
            .getClasses(any(OntModel.class));
    }
}
