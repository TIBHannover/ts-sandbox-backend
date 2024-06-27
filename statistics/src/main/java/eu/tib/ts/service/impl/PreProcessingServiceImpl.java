package eu.tib.ts.service.impl;

import eu.tib.ts.configuration.OntologiesProcessingConfig;
import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.controller.dto.SourceOntologyObjectSetModel;
import eu.tib.ts.controller.dto.TargetOntologyObjectSetModel;
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAutoConfiguration
public class PreProcessingServiceImpl implements PreProcessingService {
    private final TsRepository tsRepository;
    private final ProcessedOntologyService processedOntologyService;
    private final PreProcessingOntologyService preProcessingOntologyService;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final PreProcessingMappingService preProcessingMappingService;

    private final ProcessedMappingService processedMappingService;

    OWLOntologyManager ontologyManager;

    OntologiesProcessingConfig ontologiesProcessingConfig;


    @Autowired
    public PreProcessingServiceImpl(TsRepository tsRepository,
                                    ProcessedOntologyService processedOntologyService,
                                    ProcessedMappingService processedMappingService,
                                    PreProcessingOntologyService preProcessingOntologyService,
                                    PreProcessingMappingService preProcessingMappingService,
                                    SequenceGeneratorService sequenceGeneratorService,
                                    OntologiesProcessingConfig ontologiesProcessingConfig) {
        this.tsRepository = tsRepository;
        this.preProcessingOntologyService = preProcessingOntologyService;
        this.preProcessingMappingService = preProcessingMappingService;
        this.processedOntologyService = processedOntologyService;
        this.processedMappingService = processedMappingService;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.ontologiesProcessingConfig = ontologiesProcessingConfig;

        log.info("PreProcessingServiceImpl constructor : ");

    }

    @Override
    public void doPreProcessing() {

        List<TsOntology> tsOntologies = tsRepository.getOntologies();

        log.info("Terminology service ontology list: ");

        int ont_num = 1;
        for(TsOntology ts: tsOntologies){

            log.info(ont_num++ + ". " + ts.getOntologyId() + " , "+  ts.getUri());
        }

        long startTime = System.currentTimeMillis();

//      List<ProcessedOntology> processedOntologies = processedOntologyService.findAll();
//        List<TsOntology> unprocessedOntologies = tsOntologies.stream()
//                .filter(tsOntology -> !ontologyExists(tsOntology, processedOntologyService.findAll()))
//                .filter(tsOntology -> !ontologiesProcessingConfig.getOntologies().contains(tsOntology.getOntologyId().toLowerCase()))
                /**
                 * changed toList()
                 */
//                .collect(Collectors.toList());

/*
Commented code below stpres TIB TS ontologies into MongoDB
 */
//        log.info("Pre-processing ontologies is done in {} ms", System.currentTimeMillis() - startTime);
//        int count = 1;
//        log.info("Pre-processing starts");
//        log.info("Titled : " + tsOntologies.get(0).getTitle());
//        for (TsOntology tsOntology : unprocessedOntologies) {
//            String fileLocation = tsOntology.getConfig().getFileLocation();
//            String title = tsOntology.getConfig().getTitle();
//            if (title.equals("") || title.equals("null")) {
//                title = "no title found";
//            }
//
//            long startRead = System.currentTimeMillis();
//
//            log.info("Titled : " + title);
//
//            log.info("Titled : inner loop " + title);
//
//            ProcessedOntology processedOntology =
//                    preProcessingOntologyService.preProcess(Optional.of(tsOntology), fileLocation, title);
//
//            long endRead = System.currentTimeMillis();
//
//            log.debug("{} {} {} ms", tsOntology.getOntologyId(), fileLocation, endRead - startRead);
//
//            processedOntology.setId(sequenceGeneratorService.getSequenceNumber(ProcessedOntology.SEQUENCE_NAME));
//            processedOntologyService.save(processedOntology);
//            count++;
//        }

        log.info("Start mappings between pairs of ontologies brouped by source ontology :");

        long mappingStartTime = System.currentTimeMillis();

        log.info("mapping start time: " +mappingStartTime);

//        int unproceesedOntologySize= unprocessedOntologies.size();
          int unproceesedOntologySize = tsOntologies.size();

        int iteration =1;

        for(int i=0;i<unproceesedOntologySize;i++) {

            Set<TargetOntologyObjectSetModel> targetOntologyObjectSetModelSet = new HashSet<>();

            Set<SourceOntologyObjectSetModel> sourceOntology = new HashSet<>();
            SourceOntologyObjectSetModel sourceOntologyObjectSetModel = new SourceOntologyObjectSetModel();

            int numberOfTargetOntologies = 0;

            for (int j = i + 1; j <= unproceesedOntologySize -1 ; j++) {

                iteration = iteration +1;

                long mappingForOneOntologyPairStartTime = System.currentTimeMillis();

                ontologyManager= OWLManager.createOWLOntologyManager();

                try {

                    log.info("---- number of ontologies: " + unproceesedOntologySize );
                    log.info("---- iteration: " + iteration);
//                  log.info(i + ". ---- source ontology uri: " + unprocessedOntologies.get(i).getUri());
//                  log.info(j+ ". ---- target ontology uri: " + unprocessedOntologies.get(j).getUri());

                    log.info(i + ". ---- source ontology uri: " + tsOntologies.get(i).getUri());
                    log.info(j+ ". ---- target ontology uri: " + tsOntologies.get(j).getUri());

                    LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(ontologyManager.loadOntology(IRI.create(
                            tsOntologies.get(i).getUri())), ontologyManager.loadOntology(IRI.create(
                            tsOntologies.get(j).getUri())));

                    /**
                     * gets mappings between pairs of ontologies
                     */
                    Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();

                    /**
                     * gets conflictive mappings between pairs of ontologies
                     */
                    Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

                    if(!logmap2Mappings.isEmpty() || !conflictiveLogmap2Mappings.isEmpty()) {

                        OntologyDto sourceOnt = OntologyDto.builder()
                                .ontologyId(tsOntologies.get(i).getOntologyId())
                                .uri(tsOntologies.get(i).getUri())
                                .title(tsOntologies.get(i).getTitle())
                                .collection(tsOntologies.get(i).getCollection())
                                .build();

                        Set<OntologyDto> sourceOntologySet = new HashSet<>();
                        sourceOntologySet.add(sourceOnt);

                        sourceOntologyObjectSetModel.setId(sourceOnt.getId());
                        sourceOntologyObjectSetModel.setCollection(sourceOnt.getCollection());
                        sourceOntologyObjectSetModel.setOntologyId(sourceOnt.getOntologyId());
                        sourceOntologyObjectSetModel.setUri(sourceOnt.getUri());
                        sourceOntologyObjectSetModel.setTitle(sourceOnt.getTitle());

                        sourceOntology.add(sourceOntologyObjectSetModel);

                        OntologyDto targetOntology = OntologyDto.builder()
                                .ontologyId(tsOntologies.get(j).getOntologyId())
                                .uri(tsOntologies.get(j).getUri())
                                .title(tsOntologies.get(j).getTitle())
                                .collection(tsOntologies.get(j).getCollection())
                                .build();

                        Set<OntologyDto> targetOntologySet = new HashSet<>();

                        targetOntologySet.add(targetOntology);

                        TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();
                        targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);
                        targetOntologyObjectSetModel.setNumberOfMappings(logmap2Mappings.size());
                        targetOntologyObjectSetModel.setNumberOfConflictiveMappings(conflictiveLogmap2Mappings.size());

                        targetOntologyObjectSetModel.setMappingList(getMappingList(logmap2Mappings));
                        targetOntologyObjectSetModel.setConflictiveMappingsList(getMappingList(conflictiveLogmap2Mappings));

                        targetOntologyObjectSetModelSet.add(targetOntologyObjectSetModel);

                        numberOfTargetOntologies++;

                    }

                    log.info("|----- number of mappings: "+ logmap2Mappings.size() + " number of conflictive mappings: "+ conflictiveLogmap2Mappings.size());
                    log.info("!----- mapping between {} and {} ontologies is completed in {} ms",
                            tsOntologies.get(i).getUri(),
                            tsOntologies.get(j).getUri(),
                            System.currentTimeMillis() - mappingForOneOntologyPairStartTime);

                }catch(Exception e){

                log.error("Mapping exception happened: " + e.getMessage());

                }

            }

            if(numberOfTargetOntologies >0) {

                ProcessedMapping processedMappingGroupedBySourceOntology =
                        preProcessingMappingService.preProcessGroupedBySourceOntology(sourceOntology ,numberOfTargetOntologies, targetOntologyObjectSetModelSet);

                processedMappingGroupedBySourceOntology.setId(sequenceGeneratorService.getSequenceNumber(ProcessedMapping.SEQUENCE_NAME));

                /**
                 * Save mappings to MongoDB
                 */
                processedMappingService.save(processedMappingGroupedBySourceOntology);
            }
        }

        log.info("---- number of preprocessed ontologies: {} ", tsOntologies.size());
        log.info("---- all mappings are done in {} ms", System.currentTimeMillis() - mappingStartTime);

    }

    /**
     * stores information about mapping list (both type of mappings) in a Set of mapping object set model
     * @param logmap2MappingsSet
     * @return mappingList
     *
     */
    private Set<MappingObjectSetModel> getMappingList (Set<MappingObjectStr> logmap2MappingsSet){

        Set<MappingObjectSetModel> mappingList = new HashSet<>();

        for(MappingObjectStr mos: logmap2MappingsSet) {

            MappingObjectSetModel mappingObjectSetModel = new MappingObjectSetModel();

            mappingObjectSetModel.setSourceIRI(mos.getIRIStrEnt1());
            mappingObjectSetModel.setMappingDirection(mos.getMappingDirection());
            mappingObjectSetModel.setTargetIRI(mos.getIRIStrEnt2());
            mappingObjectSetModel.setTypeOfMapping(mos.getTypeOfMapping());
            mappingObjectSetModel.setConfidence(mos.getConfidence());
            mappingObjectSetModel.setStructuralConfidenceMapping(mos.getStructuralConfidenceMapping());

            mappingList.add(mappingObjectSetModel);
        }

        return mappingList;
    }

    private boolean ontologyExists(TsOntology tsOntology, List<ProcessedOntology> processedOntologies) {
        return processedOntologies.stream()
                .anyMatch(ont -> ont.equalsTsOntology(tsOntology));
    }
}