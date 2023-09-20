package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.io.File;
import java.io.InputStream;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
@Slf4j
@Service
@EnableAutoConfiguration
public class PreProcessingServiceImpl implements PreProcessingService {
    private final TsRepository tsRepository;
    private final ProcessedOntologyService processedOntologyService;
    private final PreProcessingOntologyService preProcessingOntologyService;

    private final SequenceGeneratorService sequenceGeneratorService;
    private final List<String> skipList;

    private final PreProcessingMappingService preProcessingMappingService;

    private final ProcessedMappingService processedMappingService;

    OWLOntologyManager ontologyManager;

    OWLOntologyManager sourceOntologyManager;
    OWLOntologyManager targetOntologyManager;


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

        log.info("PreProcessingServiceImpl constructor : ");

    }

    @Override
    public void doPreProcessing() {
        List<TsOntology> tsOntologies = tsRepository.getOntologies();
        log.info("Titled doPreProcessing:");

        List<ProcessedOntology> processedOntologies = processedOntologyService.findAll();

        log.info("Titled : third line " + tsOntologies);

        List<TsOntology> unprocessedOntologies = tsOntologies.stream()
                .filter(tsOntology -> !ontologyExists(tsOntology, processedOntologies))
                .filter(tsOntology -> !skipList.contains(tsOntology.getOntologyId().toLowerCase()))
                .collect(Collectors.toList());

        int count = 1;
        log.info("Pre-processing starts");
        long startTime = System.currentTimeMillis();

        log.info("Titled : " + tsOntologies.get(0).getTitle());

        for (TsOntology tsOntology : unprocessedOntologies) {

            String fileLocation = tsOntology.getConfig().getFileLocation();
            String title = tsOntology.getConfig().getTitle();
            if (title.equals("") || title.equals("null")) {
                title = "no title found";
            }

            long startRead = System.currentTimeMillis();

            log.info("Titled : " + title);

            log.info("Titled : inner loop " + title);

            ProcessedOntology processedOntology =
                    preProcessingOntologyService.preProcess(Optional.of(tsOntology), fileLocation, title);

            long endRead = System.currentTimeMillis();

            log.debug("{} {} {} ms", tsOntology.getOntologyId(), fileLocation, endRead - startRead);

            processedOntology.setId(sequenceGeneratorService.getSequenceNumber(ProcessedOntology.SEQUENCE_NAME));
            processedOntologyService.save(processedOntology);
            count++;

        }

        log.info("Titled : after loop");

        log.info("Pre-processing done in {} ms", System.currentTimeMillis() - startTime);

        log.info("Saved {} ontologies", count);

        log.info("Mappings between pairs of ontologies start:");


//        for(int i=0;i<unprocessedOntologies.size();i++) {
//
//            for (int j = i + 1; j < unprocessedOntologies.size()+1; j++) {
//
//                ontologyManager= OWLManager.createOWLOntologyManager();
//
//                try {
//
//                    /**
//                     *
//                     * Calculates mappings between all ontologies pair within one collection.
//                     *
//                     */
//
//                    LogMap2_Matcher logmap2 = new LogMap2_Matcher(ontologyManager.loadOntology(IRI.create(
//                            unprocessedOntologies.get(i).getUri())), ontologyManager.loadOntology(IRI.create(
//                            unprocessedOntologies.get(j).getUri())));
//
//                    /**
//                     * gets mappings between pairs of ontologies
//                     */
//                    Set<MappingObjectStr> logmap2Mappings = logmap2.getLogmap2_Mappings();
//
//                    /**
//                     * gets conflictive mappings between pairs of ontologies
//                     */
//                    Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2.getLogmap2_ConflictiveMappings();
//
//                    /**
//                     * adds information about source ontology in ontology dto
//                     */
//                    OntologyDto sourceOntology = OntologyDto.builder()
//                            .ontologyId(unprocessedOntologies.get(i).getOntologyId())
//                            .uri(unprocessedOntologies.get(i).getUri())
//                            .title(unprocessedOntologies.get(i).getTitle())
//                            .collection(unprocessedOntologies.get(i).getCollection())
//                            .build();
//
//                    Set<OntologyDto> sourceOntologySet = new HashSet<>();
//
//                    sourceOntologySet.add(sourceOntology);
//
//                    /**
//                     * adds information about target ontology into ontology dto.
//                     */
//                    OntologyDto targetOntology = OntologyDto.builder()
//                            .ontologyId(unprocessedOntologies.get(j).getOntologyId())
//                            .uri(unprocessedOntologies.get(j).getUri())
//                            .title(unprocessedOntologies.get(j).getTitle())
//                            .collection(unprocessedOntologies.get(j).getCollection())
//                            .build();
//
//                    Set<OntologyDto> targetOntologySet = new HashSet<>();
//                    targetOntologySet.add(targetOntology);
//
//
//                    ProcessedMapping processedMapping =
//                            preProcessingMappingService.preProcess(sourceOntologySet,
//                                    targetOntologySet, logmap2Mappings.size(),
//                                    conflictiveLogmap2Mappings.size(), getMappingList(logmap2Mappings),getMappingList(conflictiveLogmap2Mappings));
//
//                    processedMapping.setId(sequenceGeneratorService.getSequenceNumber(ProcessedMapping.SEQUENCE_NAME));
//                    processedMappingService.save(processedMapping);
//
//                }catch(Exception e){
//
//                    e.printStackTrace();
//
//                }
//            }
//        }

        log.info("Start mappings between pairs of ontologies brouped by source ontology :");

        for(int i=0;i<unprocessedOntologies.size();i++) {

            sourceOntologyManager= OWLManager.createOWLOntologyManager();

            Set<TargetOntologyObjectSetModel> targetOntologyObjectSetModelSet = new HashSet<TargetOntologyObjectSetModel>();

            int numberOfTargetOntologies = 0;

            Set<OntologyDto> sourceOntologyGrouppedSet = new HashSet<>();

            for (int j = i + 1; j < unprocessedOntologies.size() + 1; j++) {

                targetOntologyManager= OWLManager.createOWLOntologyManager();

                try {

                    LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(sourceOntologyManager.loadOntology(IRI.create(
                            unprocessedOntologies.get(i).getUri())), targetOntologyManager.loadOntology(IRI.create(
                            unprocessedOntologies.get(j).getUri())));

                    /**
                     * gets mappings between pairs of ontologies
                     */
                    Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();

                    /**
                     * gets conflictive mappings between pairs of ontologies
                     */
                    Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

                    if(logmap2Mappings.size() >0 || conflictiveLogmap2Mappings.size()>0) {

                        OntologyDto sourceOntology = OntologyDto.builder()
                                .ontologyId(unprocessedOntologies.get(i).getOntologyId())
                                .uri(unprocessedOntologies.get(i).getUri())
                                .title(unprocessedOntologies.get(i).getTitle())
                                .collection(unprocessedOntologies.get(i).getCollection())
                                .build();

                        Set<OntologyDto> sourceOntologySet = new HashSet<>();
                        sourceOntologySet.add(sourceOntology);

                        sourceOntologyGrouppedSet.add(sourceOntology);

                        OntologyDto targetOntology = OntologyDto.builder()
                                .ontologyId(unprocessedOntologies.get(j).getOntologyId())
                                .uri(unprocessedOntologies.get(j).getUri())
                                .title(unprocessedOntologies.get(j).getTitle())
                                .collection(unprocessedOntologies.get(j).getCollection())
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

                }catch(Exception e){

                    e.printStackTrace();

                }

            }

            for(OntologyDto ontologyDto: sourceOntologyGrouppedSet) {

                log.info("ontologyDto.getId() :" + ontologyDto.getId() );
                log.info("ontologyDto.getOntologyId() :" + ontologyDto.getOntologyId() );

            }
            log.info("number of target ontologies :" + numberOfTargetOntologies);

            log.info("target ontologies:" );

            for(TargetOntologyObjectSetModel targetOntologyObjectSetModel: targetOntologyObjectSetModelSet) {

                for(OntologyDto targetOnt: targetOntologyObjectSetModel.getTargetOntology()){

                    log.info("targetOnt.getId() : " + targetOnt.getId() );
                    log.info("targetOnt.getOntologyId() : " + targetOnt.getOntologyId() );
                }

            }


            if(numberOfTargetOntologies >0) {

                ProcessedMapping processedMappingGroupedBySourceOntology =
                        preProcessingMappingService.preProcessGroupedBySourceOntology(sourceOntologyGrouppedSet, numberOfTargetOntologies, targetOntologyObjectSetModelSet);

                processedMappingGroupedBySourceOntology.setId(sequenceGeneratorService.getSequenceNumber(ProcessedMapping.SEQUENCE_NAME));
                processedMappingService.save(processedMappingGroupedBySourceOntology);
            }
        }
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
