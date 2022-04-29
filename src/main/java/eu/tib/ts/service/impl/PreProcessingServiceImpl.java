package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.PreProcessingOntologyService;
import eu.tib.ts.service.PreProcessingService;
import eu.tib.ts.service.ProcessedOntologyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PreProcessingServiceImpl implements PreProcessingService {
    private final TsRepository tsRepository;
    private final ProcessedOntologyService processedOntologyService;
    private final PreProcessingOntologyService preProcessingOntologyService;

    private final List<String> skipList;

    @Autowired
    public PreProcessingServiceImpl(TsRepository tsRepository,
                                    ProcessedOntologyService processedOntologyService,
                                    PreProcessingOntologyService preProcessingOntologyService,
                                    @Value("#{'${skip.ontologies.processing}'.split(',')}")
                                        List<String> skipList) {
        this.tsRepository = tsRepository;
        this.preProcessingOntologyService = preProcessingOntologyService;
        this.processedOntologyService = processedOntologyService;
        this.skipList = skipList;
    }

    @Override
    public void doPreProcessing() {
        List<TsOntology> tsOntologies = tsRepository.getOntologies();
        List<ProcessedOntology> processedOntologies = processedOntologyService.findAll();

        List<TsOntology> unprocessedOntologies = tsOntologies.stream()
            .filter(tsOntology -> !ontologyExists(tsOntology, processedOntologies))
            .filter(tsOntology -> !skipList.contains(tsOntology.getOntologyId().toLowerCase()))
            .collect(Collectors.toList());

        int count = 0;
        log.info("Pre-processing starts");
        long startTime = System.currentTimeMillis();

        for (TsOntology tsOntology : unprocessedOntologies) {
            String fileLocation = tsOntology.getConfig().getFileLocation();
            long startRead = System.currentTimeMillis();

            ProcessedOntology processedOntology =
                preProcessingOntologyService.preProcess(Optional.of(tsOntology), fileLocation);

            long endRead = System.currentTimeMillis();
            log.debug("{} {} {} ms", tsOntology.getOntologyId(), fileLocation, endRead - startRead);

            processedOntologyService.save(processedOntology);
            count++;
        }
        log.info("Pre-processing done in {} ms", System.currentTimeMillis() - startTime);
        log.info("Saved {} ontologies", count);
    }

    private boolean ontologyExists(TsOntology tsOntology, List<ProcessedOntology> processedOntologies) {
        return processedOntologies.stream()
            .anyMatch(ont -> ont.equalsTsOntology(tsOntology));
    }
}
