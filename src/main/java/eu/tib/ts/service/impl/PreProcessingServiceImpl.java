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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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

        int count = 0;
        log.debug("Pre-processing starts");
        long startTime = System.currentTimeMillis();
        for (TsOntology tsOntology : tsOntologies) {
            if (ontologyExists(tsOntology, processedOntologies)) {
                continue;
            }
            String fileLocation = tsOntology.getConfig().getFileLocation();
            long startRead = System.currentTimeMillis();

            OntModel ontModel = null;
            try {
                ontModel = ontologyReadService.readOntology(fileLocation);
            } catch (Exception e) {
                log.error("Could not read {} {}", fileLocation, e.getLocalizedMessage());
            }

            if (Objects.isNull(ontModel)) {
                continue;
            }

            ProcessedOntology processedOntology = ProcessedOntology.builder()
                .ontologyId(tsOntology.getOntologyId())
                .classes(ontologyTraverseService.getClasses(ontModel))
                .imports(ontologyTraverseService.getImports(ontModel))
                .properties(ontologyTraverseService.getProperties(ontModel))
                .namespaces(ontologyTraverseService.getNamespaces(ontModel))
                .uri(tsOntology.getUri())
                .build();

            long endRead = System.currentTimeMillis();
            log.debug("{} {} {} ms", tsOntology.getOntologyId(), fileLocation, endRead - startRead);
            processedOntologyService.save(processedOntology);
            count++;
        }
        log.debug("Pre-processing done in {} ms", System.currentTimeMillis() - startTime);
        log.info("Saved {} ontologies", count);
    }

    private boolean ontologyExists(TsOntology tsOntology, List<ProcessedOntology> processedOntologies) {
        return processedOntologies.stream()
            .anyMatch(ont -> ont.equalsTsOntology(tsOntology));
    }
}
