package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.controller.dto.TargetOntologyObjectSetModel;
import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.*;
import eu.tib.ts.repository.ProcessedMongoOntologyRepository;
import eu.tib.ts.service.ExternalMappingService;
import eu.tib.ts.service.OntologyFilterService;

import eu.tib.ts.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@EnableAutoConfiguration
public class ExternalMappingServiceImpl implements ExternalMappingService {

    private final ProcessedMongoOntologyRepository ProcessedMongoOntologyRepository;

    private final OntologyFilterService ontologyFilterService;
    OWLOntologyManager ontologyManager;

    @Autowired
    protected ExternalMappingServiceImpl(
            ProcessedMongoOntologyRepository processedMongoOntologyRepository,
            OntologyFilterService ontologyFilterService
    ){

        this.ProcessedMongoOntologyRepository=processedMongoOntologyRepository;
        this.ontologyFilterService=ontologyFilterService;

    }
    @Override
    public <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology, Optional<List<String>> ids, Pageable pageable) {

        log.info("started mappings computation for the following ontologies: ");
        log.info("source ontology: " + ontology.getOntologyId());

        List<ProcessedOntology> processedOntologies = ids.isPresent()
                ? getProcessedOntologies(ids.get())
                : getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        log.info("target ontologies list:");

        for(ProcessedOntology po: processedOntologies){
            log.info(po.getOntologyId());
        }

        ProcessedOntology ont2 = ProcessedOntology.of(ontology);

        List<ExternalMapping> externalMappingList = new ArrayList<>();

//        Set<OntologyPair> set = new HashSet<>();

        int numberOfTargetOntologies = processedOntologies.size();

        Set<TargetOntologyObjectSetModel> targetOntologyList = new HashSet<TargetOntologyObjectSetModel>();

        int numberOfMappingsProcessed = 0;

        //filteredTSOntologies
        for (ProcessedOntology ont1 : processedOntologies) {

            log.info("mapping for ontology : " + ont1.getOntologyId());
            log.info("target ontology uri: " + ont1.getUri());

            /**
             * disallow mapping computation between the same URLs
             */
            if(ont1.getUri().equals(ont2.getUri())) continue;

            ontologyManager= OWLManager.createOWLOntologyManager();

//            OntologyPair pair = OntologyPair.of(ont2, ont1);

            /**
             * Skips to produce mappings between ontologies that have equal ids
             */
//            if (ont1.equalsTsOntology(ont2) || set.contains(pair.inverted())) {
//                continue;
//            }

//            set.add(pair);

//            try {

            TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();

                        try {
                            /**
                             * generate first and second random numbers
                             */
                            Random r_1 = SecureRandom.getInstanceStrong();
                            Random r_2 = SecureRandom.getInstanceStrong();

                            long id = r_1.nextLong() * r_2.nextLong();
                            targetOntologyObjectSetModel.setId(id);

                        }catch (Exception e) {

                            log.info("Source random get instance exception: " + e.getMessage());

                        }

                /**
                 * target ontology hashset
                 */
                Set<OntologyDto> targetOntologySet = new HashSet<>();

            log.info("target ontology id: " + ont1.getOntologyId());
            log.info("target ontology uri: " + ont1.getUri());
            log.info("target ontology title: " + ont1.getTitle());
            log.info("target ontology collection: " + ont1.getCollection());
                /**
                 * target ontology dto from terminology service (localhost: Docker)
                 */
                OntologyDto targetTSOntDto = OntologyDto.builder()
                        .ontologyId(ont1.getOntologyId())
                        .uri(ont1.getUri())
                        .title(ont1.getTitle())
                        .collection(ont1.getCollection())
                        .build();

                targetOntologySet.add(targetTSOntDto);

                /**
                 * target ontology set
                 */
                targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);



                try {

                LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(ontologyManager.loadOntology(IRI.create(
                        ont2.getUri())), ontologyManager.loadOntology(IRI.create(
                        ont1.getUri())));

                Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();

                Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

                if(!logmap2Mappings.isEmpty() || !conflictiveLogmap2Mappings.isEmpty()) {

                    /**
                     *
                     * Number of mappings
                     * Number of conflictive mappings
                     */
                    targetOntologyObjectSetModel.setNumberOfMappings(logmap2Mappings.size());
                    targetOntologyObjectSetModel.setNumberOfConflictiveMappings(conflictiveLogmap2Mappings.size());

                    Set<MappingObjectSetModel> mappingList = new HashSet<MappingObjectSetModel>();

                    /**
                     * Store mappings information in target ontology object set model
                     */
                    getExternalMappings(mappingList, logmap2Mappings);

                    /**
                     * mapping list
                     */
                    targetOntologyObjectSetModel.setMappingList(mappingList);

                    Set<MappingObjectSetModel> conflictiveMappingList = new HashSet<MappingObjectSetModel>();

                    /**
                     * Stores conflictive mappings information in target ontology object set model
                     */
                    getExternalMappings(conflictiveMappingList, conflictiveLogmap2Mappings);

                    /**
                     * conflictive mappings list
                     */
                    targetOntologyObjectSetModel.setConflictiveMappingsList(conflictiveMappingList);



                }
//                else {
//
//                    OntologyDto targetTSOntDtoNew = OntologyDto.builder()
//                            .ontologyId(ont1.getOntologyId())
//                            .uri(ont1.getUri())
//                            .title(ont1.getTitle())
//                            .collection(ont1.getCollection())
//                            .build();
//
//                    targetOntologySet.add(targetTSOntDtoNew);
//                    /**
//                     *
//                     */
//                    Set<MappingObjectSetModel> emptyMappingList = Collections.emptySet();
//                    Set<MappingObjectSetModel> emptyConflictiveMappingList = Collections.emptySet();;
//
//                    /**
//                     * target ontology set
//                     */
//                    targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);
//
//                    /**
//                     * if mappings and conflictive mappings set are null then we set these sets as empty
//                     */
//                    targetOntologyObjectSetModel.setMappingList(emptyMappingList);
//                    targetOntologyObjectSetModel.setMappingList(emptyConflictiveMappingList);
//
//                    targetOntologyList.add(targetOntologyObjectSetModel);
//                }

            }catch(Exception e){

            log.error("Mapping exception: " + e.getMessage());

            }

            targetOntologyList.add(targetOntologyObjectSetModel);

        numberOfMappingsProcessed++;

        }


        ExternalMapping externalMapping = processExternalMapping(ont2, numberOfTargetOntologies, targetOntologyList);

        externalMappingList.add(externalMapping);

        log.info("number of mappings processed: " + numberOfMappingsProcessed);

         return PageUtils.toPage(externalMappingList, pageable);

    }

    private void getExternalMappings(Set<MappingObjectSetModel> mappingList, Set<MappingObjectStr> logmap2Mappings) {

        for (MappingObjectStr mappingObjectStr : logmap2Mappings) {

            MappingObjectSetModel mappingObjectSetModel = new MappingObjectSetModel();

            mappingObjectSetModel.setSourceIRI(mappingObjectStr.getIRIStrEnt1());
            mappingObjectSetModel.setTargetIRI(mappingObjectStr.getIRIStrEnt2());
            mappingObjectSetModel.setMappingDirection(mappingObjectStr.getMappingDirection());
            mappingObjectSetModel.setTypeOfMapping(mappingObjectStr.getTypeOfMapping());
            mappingObjectSetModel.setStructuralConfidenceMapping(mappingObjectStr.getStructuralConfidenceMapping());
            mappingObjectSetModel.setConfidence(mappingObjectStr.getConfidence());

            mappingList.add(mappingObjectSetModel);
        }
    }

    private ExternalMapping processExternalMapping(ProcessedOntology ont1,
                                            int numberOfTargetOntologies,
                                            Set<TargetOntologyObjectSetModel> targetOntologyList) {
        return ExternalMapping.builder()
                .mappingId(UUID.randomUUID().toString())
                .sourceOntologyURI(ont1.getUri())
                .numberOfTargetOntologies(numberOfTargetOntologies)
                .targetOntologyList(targetOntologyList)
                .build();
    }

    private List<ProcessedOntology> getProcessedOntologies(List<String> ids) {

        return ProcessedMongoOntologyRepository.findByOntologyIdIn(ids);

    }

    private List<ProcessedOntology> getProcessedOntologies() {

        return StreamSupport.stream(ProcessedMongoOntologyRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ProcessedOntology::getOntologyId))
                .collect(Collectors.toList());

    }


}
