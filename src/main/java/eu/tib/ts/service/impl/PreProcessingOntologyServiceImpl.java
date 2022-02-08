package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.service.OntologyReadService;
import eu.tib.ts.service.OntologyTraverseService;
import eu.tib.ts.service.PreProcessingOntologyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntModel;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class PreProcessingOntologyServiceImpl implements PreProcessingOntologyService {
    public static final String EXTERNAL = "external";
    private final OntologyReadService ontologyReadService;
    private final OntologyTraverseService ontologyTraverseService;

    @Autowired
    public PreProcessingOntologyServiceImpl(OntologyReadService ontologyReadService,
                                            OntologyTraverseService ontologyTraverseService) {
        this.ontologyReadService = ontologyReadService;
        this.ontologyTraverseService = ontologyTraverseService;
    }

    @Override
    public ProcessedOntology preProcess(Optional<TsOntology> tsOntology, String fileLocation) {
        log.debug("Start pre-processing {}", fileLocation);

        OntModel ontModel = null;
        OWLOntology owlOntology = null;
        try {
            ontModel = ontologyReadService.readOntologyWithJenaApi(fileLocation);
        } catch (Exception e) {
            log.error("Could not read with Jena API {} {}", fileLocation, e.getLocalizedMessage());
        }

        try {
            owlOntology = ontologyReadService.readOntologyWithOwlApi(fileLocation);
        } catch (Exception e) {
            log.error("Could not read with OWL API {} {}", fileLocation, e.getLocalizedMessage());
        }

        return buildOntology(tsOntology, owlOntology, ontModel, fileLocation);
    }

    private ProcessedOntology buildOntology(Optional<TsOntology> tsOntology,
                                            OWLOntology owlOntology,
                                            OntModel ontModel,
                                            String uri) {
        Set<String> classes = ontologyTraverseService.getClasses(ontModel);
        if (CollectionUtils.isEmpty(classes)) {
            classes = ontologyTraverseService.getClasses(owlOntology);
        }

        return ProcessedOntology.builder()
            .ontologyId(tsOntology.map(TsOntology::getOntologyId).orElse(EXTERNAL))
            .classes(classes)
            .imports(ontologyTraverseService.getImports(owlOntology))
            .properties(ontologyTraverseService.getProperties(ontModel))
            .namespaces(ontologyTraverseService.getNamespaces(owlOntology))
            .individuals(ontologyTraverseService.getIndividuals(owlOntology))
            .collection(tsOntology.map(TsOntology::getCollection).orElse(Collections.emptySet()))
            .uri(uri)
            .build();
    }
}
