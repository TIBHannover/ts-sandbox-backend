package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.*;
import lombok.extern.slf4j.Slf4j;

import org.semanticweb.owlapi.apibinding.OWLManager;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PreProcessingServiceImpl implements PreProcessingService {
    private final TsRepository tsRepository;
    private final ProcessedOntologyService processedOntologyService;
    private final PreProcessingOntologyService preProcessingOntologyService;

    private final SequenceGeneratorService sequenceGeneratorService;
    private final List<String> skipList;

    private final PreProcessingMappingService preProcessingMappingService;

    private final ProcessedMappingService processedMappingService;


    OWLOntologyManager ontologyManager;

    @Autowired
    public PreProcessingServiceImpl(TsRepository tsRepository,
                                    ProcessedOntologyService processedOntologyService,
                                    ProcessedMappingService processedMappingService,
                                    PreProcessingOntologyService preProcessingOntologyService,
                                    PreProcessingMappingService preProcessingMappingService,
                                    SequenceGeneratorService sequenceGeneratorService,
                                    @Value("#{'${skip.ontologies.processing}'.split(',')}")
                                    List<String> skipList) {
        this.tsRepository = tsRepository;
        this.preProcessingOntologyService = preProcessingOntologyService;
        this.preProcessingMappingService = preProcessingMappingService;
        this.processedOntologyService = processedOntologyService;
        this.processedMappingService = processedMappingService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.skipList = skipList;
        System.out.println("PreProcessingServiceImpl constructor : ");

    }

    @Override
    public void doPreProcessing() {
        List<TsOntology> tsOntologies = tsRepository.getOntologies();
        System.out.println("Titled doPreProcessing:");

        List<ProcessedOntology> processedOntologies = processedOntologyService.findAll();

        System.out.println("Titled : third line " + tsOntologies);

        List<TsOntology> unprocessedOntologies = tsOntologies.stream()
                .filter(tsOntology -> !ontologyExists(tsOntology, processedOntologies))
                .filter(tsOntology -> !skipList.contains(tsOntology.getOntologyId().toLowerCase()))
                .collect(Collectors.toList());

        int count = 1;
        log.info("Pre-processing starts");
        long startTime = System.currentTimeMillis();

        System.out.println("Titled : " + tsOntologies.get(0).getTitle());

        for (TsOntology tsOntology : unprocessedOntologies) {

            String fileLocation = tsOntology.getConfig().getFileLocation();
            String title = tsOntology.getConfig().getTitle();
            if (title.equals("") || title.equals("null")) {
                title = "no title found";
            }

            long startRead = System.currentTimeMillis();

            log.info("Titled : " + title);

            System.out.println("Titled : inner loop " + title);

            ProcessedOntology processedOntology =
                    preProcessingOntologyService.preProcess(Optional.of(tsOntology), fileLocation, title);

            long endRead = System.currentTimeMillis();

            log.debug("{} {} {} ms", tsOntology.getOntologyId(), fileLocation, endRead - startRead);

//          ProcessedOntology.builder().id().build();
            processedOntology.setId(sequenceGeneratorService.getSequenceNumber(ProcessedOntology.SEQUENCE_NAME));
            processedOntologyService.save(processedOntology);
            count++;
        }

        System.out.println("Titled : after loop");

        log.info("Pre-processing done in {} ms", System.currentTimeMillis() - startTime);

        log.info("Saved {} ontologies", count);

        log.info("List of all {} ontologies", count);

        for(TsOntology tso: unprocessedOntologies){


        log.info(tso.getOntologyId() + " , " + tso.getUri());

        }

        log.info("Mappings between ontology pairs: ");


        for(int i=0;i<unprocessedOntologies.size();i++) {

            for (int j = i + 1; j < unprocessedOntologies.size()+1; j++) {

                ontologyManager= OWLManager.createOWLOntologyManager();

                try {

                    /**
                     *
                     * Calculates mappings between all ontologies pair within one collection.
                     *
                     */

                    LogMap2_Matcher logmap2 = new LogMap2_Matcher(ontologyManager.loadOntology(IRI.create(
                            unprocessedOntologies.get(i).getUri())),ontologyManager.loadOntology(IRI.create(
                            unprocessedOntologies.get(j).getUri())));


                Set<MappingObjectStr> logmap2Mappings = logmap2.getLogmap2_Mappings();

                log.info("source ont:" + unprocessedOntologies.get(i).getUri() + " , target ont: " +
                            unprocessedOntologies.get(j).getUri() + " number of mappings: " +
                            logmap2Mappings.size());

                 ProcessedMapping processedMapping =
                    preProcessingMappingService.preProcess(logmap2Mappings, ontologyManager.loadOntology(IRI.create(
                            unprocessedOntologies.get(i).getUri())), ontologyManager.loadOntology(IRI.create(
                            unprocessedOntologies.get(j).getUri())));

//          ProcessedOntology.builder().id().build();
            processedMapping.setId(sequenceGeneratorService.getSequenceNumber(ProcessedMapping.SEQUENCE_NAME));
            processedMappingService.save(processedMapping);

            }catch(Exception e){

            e.printStackTrace();

                }
            }
        }
    }

    private boolean ontologyExists(TsOntology tsOntology, List<ProcessedOntology> processedOntologies) {
        return processedOntologies.stream()
                .anyMatch(ont -> ont.equalsTsOntology(tsOntology));
    }
}