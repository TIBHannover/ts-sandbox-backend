package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.OntologyReadService;
import eu.tib.ts.service.OntologyTraverseService;
import eu.tib.ts.service.PreProcessingService;
import eu.tib.ts.service.ProcessedOntologyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntModel;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PreProcessingServiceImpl implements PreProcessingService {
    private final TsRepository tsRepository;
    private final OntologyReadService ontologyReadService;
    private final OntologyTraverseService ontologyTraverseService;
    private final ProcessedOntologyService processedOntologyService;

    @Autowired
    public PreProcessingServiceImpl(TsRepository tsRepository,
                                    OntologyReadService ontologyReadService,
                                    OntologyTraverseService ontologyTraverseService,
                                    ProcessedOntologyService processedOntologyService) {
        this.tsRepository = tsRepository;
        this.ontologyReadService = ontologyReadService;
        this.ontologyTraverseService = ontologyTraverseService;
        this.processedOntologyService = processedOntologyService;
    }

    @Override
    public void doPreProcessing() {
        List<TsOntology> tsOntologies = tsRepository.getOntologies();
        List<ProcessedOntology> processedOntologies = processedOntologyService.findAll();

        List<TsOntology> unprocessedOntologies = tsOntologies.stream()
            .filter(tsOntology -> !ontologyExists(tsOntology, processedOntologies))
            .collect(Collectors.toList());

        int count = 0;
        log.info("Pre-processing starts");
        long startTime = System.currentTimeMillis();

        for (TsOntology tsOntology : unprocessedOntologies) {
            String fileLocation = tsOntology.getConfig().getFileLocation();
            long startRead = System.currentTimeMillis();

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
                log.error("Could not read with OWL API{} {}", fileLocation, e.getLocalizedMessage());
            }

            ProcessedOntology processedOntology = buildOntology(tsOntology, owlOntology, ontModel);

            long endRead = System.currentTimeMillis();
            log.debug("{} {} {} ms", tsOntology.getOntologyId(), fileLocation, endRead - startRead);

            processedOntologyService.save(processedOntology);
            count++;
        }
        log.info("Pre-processing done in {} ms", System.currentTimeMillis() - startTime);
        log.info("Saved {} ontologies", count);
    }

    private ProcessedOntology buildOntology(TsOntology tsOntology, OWLOntology owlOntology, OntModel ontModel) {
        Set<String> classes = ontologyTraverseService.getClasses(ontModel);
        if (CollectionUtils.isEmpty(classes)) {
            classes = ontologyTraverseService.getClasses(owlOntology);
        }

        return ProcessedOntology.builder()
            .ontologyId(tsOntology.getOntologyId())
            .classes(classes)
            .imports(ontologyTraverseService.getImports(owlOntology))
            .properties(ontologyTraverseService.getProperties(ontModel))
            .namespaces(ontologyTraverseService.getNamespaces(owlOntology))
            .individuals(ontologyTraverseService.getIndividuals(owlOntology))
            .collection(tsOntology.getCollection())
            .uri(tsOntology.getUri())
            .build();
    }

    private boolean ontologyExists(TsOntology tsOntology, List<ProcessedOntology> processedOntologies) {
        return processedOntologies.stream()
            .anyMatch(ont -> ont.equalsTsOntology(tsOntology));
    }
}
