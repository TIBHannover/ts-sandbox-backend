package eu.tib.ts.service.impl;

import eu.tib.ts.configuration.OntologiesProcessingConfig;
import eu.tib.ts.controller.dto.*;
import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.*;
import eu.tib.ts.repository.ProcessedMongoOntologyRepository;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.*;

import eu.tib.ts.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.Parameters;
import uk.ac.ox.krr.logmap2.io.OWLAlignmentFormat;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.reasoning.SatisfiabilityIntegration;
import uk.ac.ox.krr.logmap2.utilities.Utilities;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@EnableAutoConfiguration
public class ExternalMappingServiceImpl implements ExternalMappingService {

    private final ProcessedMongoOntologyRepository ProcessedMongoOntologyRepository;

    OWLOntologyManager ontologyManager;

    private final TsRepository tsRepository;
    private final ProcessedOntologyService processedOntologyService;
    OntologiesProcessingConfig ontologiesProcessingConfig;

    private final SequenceGeneratorService sequenceGeneratorService;

    private final PreProcessingOntologyService preProcessingOntologyService;

    private final OntologyStorageService ontologyStorageService;

    private final PreProcessingMappingService preProcessingMappingService;

    private final ProcessedMappingService processedMappingService;

    @Autowired
    protected ExternalMappingServiceImpl(
            ProcessedMongoOntologyRepository processedMongoOntologyRepository,
            OntologyStorageService ontologyStorageService,
            TsRepository tsRepository,
            ProcessedOntologyService processedOntologyService,
            OntologiesProcessingConfig ontologiesProcessingConfig,
            PreProcessingOntologyService preProcessingOntologyService,
            SequenceGeneratorService sequenceGeneratorService,
            PreProcessingMappingService preProcessingMappingService,
            ProcessedMappingService processedMappingService

    ){

    this.ProcessedMongoOntologyRepository=processedMongoOntologyRepository;
        this.ontologyStorageService=ontologyStorageService;
        this.tsRepository=tsRepository;
        this.processedOntologyService=processedOntologyService;
        this.ontologiesProcessingConfig=ontologiesProcessingConfig;
        this.preProcessingOntologyService=preProcessingOntologyService;
        this.sequenceGeneratorService=sequenceGeneratorService;
        this.preProcessingMappingService=preProcessingMappingService;
        this.processedMappingService = processedMappingService;
    }



    @Override
    public <T extends  ExtendedOntology> Page<ExternalMapping> getMultipartFileMappingMappingForExternalOntology(MultipartFile file,
                                                                                                                 MultipartFile[] files,
                                                                                                                 boolean sat,
                                                                                                                 Pageable pageable) throws OWLOntologyCreationException, IOException {

        OWLOntology sourceOntology = ontologyStorageService.loadOntologyIntoOWLOntologyFromMultipartFile(file);

        List<OWLOntology> owlOntologyList = new ArrayList<>();

        for(MultipartFile f: files){

            OWLOntology targetOntology = ontologyStorageService.loadOntologyIntoOWLOntologyFromMultipartFile(f);

            owlOntologyList.add(targetOntology);

        }

        List<ExternalMapping> externalMappingList = new ArrayList<>();

        int numberOfTargetOntologies = owlOntologyList.size();

        Set<TargetOntologyObjectSetModel> targetOntologyList = new HashSet<TargetOntologyObjectSetModel>();

        int numberOfMappingsProcessed = 0;

        for (OWLOntology ont1 : owlOntologyList) {

            ontologyManager= OWLManager.createOWLOntologyManager();
            
            TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();

            try {
                /**
                 * generate first and second random numbers
                 */
                Random r1 = SecureRandom.getInstanceStrong();
                Random r2 = SecureRandom.getInstanceStrong();

                long id = r1.nextLong() * r2.nextLong();
                targetOntologyObjectSetModel.setId(id);

            }catch (Exception e) {

                log.info("Source random get instance exception: " + e.getMessage());

            }
            /**
             * target ontology hashset
             */
            Set<OntologyDto> targetOntologySet = new HashSet<>();

            /**
             * target ontology dto from terminology service (localhost: Docker)
             */
            OntologyDto targetTSOntDto = OntologyDto.builder()
                    .ontologyId(ont1.getOntologyID().getOntologyIRI().get().getFragment().toString())
                    .uri(ont1.getOntologyID().getOntologyIRI().get().toURI().toString())
                    .title(ont1.getOntologyID().getOntologyIRI().get().getFragment().toString())
                    .collection(null)
                    .build();

            targetOntologySet.add(targetTSOntDto);

            /**
             * target ontology set
             */
            targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);

            try {

                /**
                 * Ontologies are loaded into LogMap Matcher as OWLOntology and not via URI.
                 * Enable  HermiT reasoner during the computation of mappings. In Parameters class reasoning is set to HermiT.
                 */
                LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(sourceOntology, ont1, Parameters.hermit);;

                Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();
                Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

                if(sat) {

                    targetOntologyObjectSetModel.setMappingException(getReasoningExplanationForMultipartOWLOntologyFile(logmap2Mappings, sourceOntology, ont1 ));

                } else {

                    targetOntologyObjectSetModel.setMappingException("Checking classes satisfiability is off");
                }

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

            }catch(Exception e){

                targetOntologyObjectSetModel.setMappingException(getExeptionMessage(e, " "));

                log.error("Mapping exception: " + e.getMessage());

            }

            targetOntologyList.add(targetOntologyObjectSetModel);

            numberOfMappingsProcessed++;

        } // end loop for all selected ontologies from tib terminology service

        ExternalMapping externalMapping = processExternalMultifilePartMapping(sourceOntology, numberOfTargetOntologies, targetOntologyList);

        externalMappingList.add(externalMapping);

        log.info("number of mappings processed: " + numberOfMappingsProcessed);

        return PageUtils.toPage(externalMappingList, pageable);

    }

    private String getReasoningExplanationForMultipartOWLOntologyFile(Set<MappingObjectStr> logmap2Mappings, OWLOntology onto2, OWLOntology onto1 ){

        try{

            OWLOntology mappingsToOWLOntology = getOWLOntology4GivenMappings(logmap2Mappings);

            try {

                /**
                 * this classs is taken from LogMap matcher library (API)
                 */
                SatisfiabilityIntegration mappingsSatChecker = new SatisfiabilityIntegration(
                        onto2,
                        onto1,
                        mappingsToOWLOntology,//mappingsToOWLOntology , //mergedOntology,
                        true,//checks classes satisfiability
                        false,//Time_Out_Class
                        false); //use factory

                if (mappingsSatChecker.hasUnsatClasses()) {

                    log.info("merged "+ onto2.getOntologyID() + " ontology, "+ onto1.getOntologyID()+" ontology and mappings ontology does not have unsatisfiable classes");

                    return "unsatisfiable classes: " + mappingsSatChecker.hasUnsatClasses() ;

                } else {

                    log.info("merged "+ onto2.getOntologyID() + " ontology, "+ onto1.getOntologyID()+" ontology and mappings ontology does not have unsatisfiable classes");

                    return "unsatisfiable classes: " + mappingsSatChecker.hasUnsatClasses();

                }

            } catch(InconsistentOntologyException e){

                log.info("merged "+ onto2.getOntologyID()+ " ontology, "+ onto1.getOntologyID()+" ontology and mappings ontology inconsistency: " + getExeptionMessage(e,""));

                return getExeptionMessage(e, " ");
            }

        }catch (OWLOntologyCreationException owlOntologyCreationException){

            log.info("owlOntologyCreationException.getLocalizedMessage(): " +  getExeptionMessage(owlOntologyCreationException,""));

            return getExeptionMessage(owlOntologyCreationException, "OWL ontology creation exception is detected:");

        } catch (Exception e) {

            String message =  new RuntimeException(e).getLocalizedMessage();

            log.info("runtime exception occurs: " + message);

            return getExeptionMessage(e," ");
        }
    }


    @Override
    public <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology, Optional<List<String>> ids, boolean sat, Pageable pageable) {

        log.info("started mappings computation for the following ontologies: ");
        log.info("source ontology: " + ontology.getUri());
        log.info("SAT selected : " + sat);

        List<ProcessedOntology> processedOntologies = ids.isPresent()
                ? getProcessedOntologies(ids.get())
                : getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {

            log.info("processedOntologies.size: " + processedOntologies.size());

            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        log.info("target ontologies list:");

        for(ProcessedOntology po: processedOntologies){
            log.info(po.getUri());
        }

        ProcessedOntology ont2 = ProcessedOntology.of(ontology);

        List<ExternalMapping> externalMappingList = new ArrayList<>();

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

            TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();

                        try {
                            /**
                             * generate first and second random numbers
                             */
                            Random r1 = SecureRandom.getInstanceStrong();
                            Random r2 = SecureRandom.getInstanceStrong();

                            long id = r1.nextLong() * r2.nextLong();
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
/**
 * Enable  HermiT reasoner during the computation of mappings. In Parameters class reasoning is set to HermiT.
 *
 */
                LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(
                        ontologyManager.loadOntology(IRI.create(
                                ont2.getUri())), ontologyManager.loadOntology(IRI.create(
                        ont1.getUri())), Parameters.hermit
                );;

                Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();
                Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

                if(sat) {

                    targetOntologyObjectSetModel.setMappingException(getReasoningExplanation(logmap2Mappings, ont2, ont1 ));

                } else {

                    targetOntologyObjectSetModel.setMappingException("Checking classes satisfiability is off");
                }

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

            }catch(Exception e){

            targetOntologyObjectSetModel.setMappingException(getExeptionMessage(e, " "));

            log.error("Mapping exception: " + e.getMessage());

            }

            targetOntologyList.add(targetOntologyObjectSetModel);

            numberOfMappingsProcessed++;

        } // end loop for all selected ontologies from tib terminology service

        ExternalMapping externalMapping = processExternalMapping(ont2, numberOfTargetOntologies, targetOntologyList);

        externalMappingList.add(externalMapping);

        log.info("number of mappings processed: " + numberOfMappingsProcessed);

         return PageUtils.toPage(externalMappingList, pageable);

    }

    /**
     * checks if ontology belongs to unique list of ontologies stored in Mongo DB
     * @param ontologyDto
     * @param uniqueTargetOntologyDtoList
     * @return
     */
    boolean existsDtoInOntologyDtoList(OntologyDto ontologyDto, List<OntologyDto> uniqueTargetOntologyDtoList){

        for(OntologyDto ont: uniqueTargetOntologyDtoList){

            if(ontologyDto.getOntologyId().equals(ont.getOntologyId())) return true ;
        }

        return false;
    }

    boolean existsOntologyIdInOntologyDtoList(String ontologyId, List<OntologyDto> uniqueTargetOntologyDtoList){

        for(OntologyDto ont: uniqueTargetOntologyDtoList){

            if(ont.getOntologyId().equals(ontologyId)) return true ;
        }

        return false;
    }


    /**
     *
     * @param ids list of ontology ids
     * @param pageable
     * @return mappings between ontology ids from parameter list and processed ontologies from MongoDB
     * @param <T>
     */
    @Override
    public <T extends ExtendedOntology> Page<ExternalMapping> getAllTIBTSOntologiesAndProcessedOntologiesInMappings(
            List<String> ids,
            Pageable pageable) {

        System.out.println("getAllTIBTSOntologiesAndProcessedOntologiesInMappings");

        /**
         * This post request terminates if parameter list is empty or null
         */
        if(ids.isEmpty()){

            log.info("parameter list is empty or null " );

            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        /**
         * Get all ontologies from TIB TS
         */
        Collection<TsOntology> terminologyServiceOntologies = tsRepository.getOntologies();

        /**
         * If set of TIB TS ontologies is empty or null then
         * the code returns empty list.
         */
        if (terminologyServiceOntologies==null || terminologyServiceOntologies.isEmpty()) {

            log.info("terminologyServiceOntologies.isEmpty(): " + terminologyServiceOntologies.isEmpty());

            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        /**
         * Get all mappimgs stored in MongoDB
         */
        List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoList =
                processedMappingService.getAllMappingsGroupedBySourceOntology();

        /**
         * If mappings stored in MongoDB are empty then this post request terminates.
         */
        if (mappingGropedBySourceOntologyDtoList.isEmpty()) {

            log.info("--Mappings grouped by source ontology is empty or null. ");

            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        /**
         * List contains all source and target ontologies used in mappings
         */
        List<OntologyDto> uniqueTargetOntologyDtoList = new ArrayList<OntologyDto>();

        /**
         * Iterates through all mappings and creates list of all source and target ontologies
         * from Mongo DB that are used in mappings.
         */
        for(MappingGropedBySourceOntologyDto mongoData: mappingGropedBySourceOntologyDtoList){

            Set<SourceOntologyObjectSetModel> sourceOntologyList = mongoData.getSourceOntology();
            Set<TargetOntologyObjectSetModel> targetOntologyList = mongoData.getTargetOntologyList();

            for(SourceOntologyObjectSetModel source: sourceOntologyList) {

                OntologyDto sourceOntDto = OntologyDto.builder()
                        .ontologyId(source.getOntologyId())
                        .uri(source.getUri())
                        .title(source.getTitle())
                        .collection(source.getCollection())
                        .build();

                uniqueTargetOntologyDtoList.add(sourceOntDto);

            }

            for(TargetOntologyObjectSetModel target: targetOntologyList){

                Set<OntologyDto> targetOntologySet = target.getTargetOntology();

                for(OntologyDto ontDto: targetOntologySet) {

              if(!existsDtoInOntologyDtoList(ontDto,uniqueTargetOntologyDtoList)){

                    uniqueTargetOntologyDtoList.add(ontDto);

                  }
                }
            }
        }

        log.info("----------------------------------------------------------");
        log.info("--list of unique (source and target) ontologies from MongoDB: " );
        int targetOntDto =1 ;
        for(OntologyDto s:  uniqueTargetOntologyDtoList){
            log.info(targetOntDto++ + ". ontology [ id: " + s.getOntologyId() +" , title: "+ s.getTitle() + " ]");
        }
        log.info("----------------------------------------------------------");

        Set<OntologyDto> newOntologySetFromParameterList = new HashSet<>();

        /**
         * creates a list of source ontologies from ontology ids available in parameter list by comparing IDs with
         * TIB TS ontology IDs
         */
        for(String id: ids){

            /**
             * checks if ontology id exists in TIB TS and does not exists in processed
             * ontology list (ontologies for which mappings are computted)
             */
            if(existsOntologyInTerminologyService(id, terminologyServiceOntologies) &&
                    !existsOntologyIdInOntologyDtoList(id,uniqueTargetOntologyDtoList)){

            log.info("--ontology id [" + id + "] belongs to TIB TS and does not belong to ontologies from Mongo DB");

                /**
                 * list to join filtered ontologies from parameter list and processed ontology from MongoDB into one list.
                 */

            for(TsOntology terminologyServiceOntology: terminologyServiceOntologies){

                /**
                 * Creates ontology dto list from TIB TS if  an ontology ID from parameter list is equal to the ontology ID
                 * from TIB TS.
                 */
                if(terminologyServiceOntology.getOntologyId().equals(id)) {

                    OntologyDto sourceTsOntDto = OntologyDto.builder()
                            .ontologyId(terminologyServiceOntology.getOntologyId())
                            .uri(terminologyServiceOntology.getUri())
                            .title(terminologyServiceOntology.getTitle())
                            .collection(terminologyServiceOntology.getCollection())
                            .build();

                newOntologySetFromParameterList.add(sourceTsOntDto);

                }

                }

            } else {

                log.info("ontology id " + id + " is ignored!") ;

            }
        }

        /**
         * terminates mappings if number of accepted
         * ontologies from parameter list is zero
         */
        if(newOntologySetFromParameterList.isEmpty()){

            log.info("number of accepted ontologies from parameter list is: " + newOntologySetFromParameterList.size());

            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        log.info("source (filtered) ontology size from TIB TS:  " + newOntologySetFromParameterList.size());
        log.info("--List of ontologies filtered from parameter list:");
        int newont =1;
        for(OntologyDto newontologyList: newOntologySetFromParameterList){

        log.info(newont++ +". --id: " + newontologyList.getOntologyId() + " , title: " + newontologyList.getTitle() +
                " , uri: " +newontologyList.getUri() + " , collection: " + newontologyList.getCollection());
        }

        log.info("Start mappings between pairs of ontologies grouped by source ontology :");

        long mappingStartTime = System.currentTimeMillis();

        log.info("--mapping start time: " +mappingStartTime);

        int count=1;

        /**
         * Mappings result that should be stored in JSON format
         */
        List<ExternalMapping> externalMappingList = new ArrayList<>();

        /**
         * list of source ontologies fitered from parameter list
         */
        log.info("--mappigns between filtered ontologies from TIB TS and processed ontologies stored in Mongo DB: ");
        for(OntologyDto sourceOntologyDto: newOntologySetFromParameterList) {

            log.info(+ count ++ +". --source ontology: "+sourceOntologyDto.getOntologyId() + " , "
                    + sourceOntologyDto.getTitle()+ " , " + sourceOntologyDto.getUri());

            Set<TargetOntologyObjectSetModel> targetOntologyObjectSetModelSet = new HashSet<>();

            Set<SourceOntologyObjectSetModel> sourceOntology = new HashSet<>();
            SourceOntologyObjectSetModel sourceOntologyObjectSetModel = new SourceOntologyObjectSetModel();

            Set<TargetOntologyObjectSetModel> targetOntologyList = new HashSet<TargetOntologyObjectSetModel>();

            int numberOfTargetOntologiesProcessed = 0;
            /**
             * We use already processed ontologies in Mongo DB as a target ontologies
             * processedTargetOntologySize
             */
            int iteration =1;

            for(OntologyDto targetOntologyDto : uniqueTargetOntologyDtoList) {

             if(sourceOntologyDto.getUri().equals(targetOntologyDto.getUri())) {

                 log.info("--source ontology uri : [ " +sourceOntologyDto.getUri() + "] is equal to target ontology uri : [ " +
                         targetOntologyDto.getUri() + " ] ");

                 continue;
             }

            long mappingForOneOntologyPairStartTime = System.currentTimeMillis();

            ontologyManager = OWLManager.createOWLOntologyManager();

            try {

            log.info(iteration + ".-- target ontology uri: " + targetOntologyDto.getUri());

            log.info("--computes mappings between ontologies: ("+ sourceOntologyDto.getOntologyId()+","+
                        targetOntologyDto.getOntologyId() +"): ");

            LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(ontologyManager.loadOntology(IRI.create(
                        sourceOntologyDto.getUri())), ontologyManager.loadOntology(IRI.create(
                        targetOntologyDto.getUri())));

            Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();
            Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

            if(!logmap2Mappings.isEmpty() || !conflictiveLogmap2Mappings.isEmpty()) {

                numberOfTargetOntologiesProcessed = numberOfTargetOntologiesProcessed +1;

                sourceOntologyObjectSetModel.setId(sourceOntologyDto.getId());
                sourceOntologyObjectSetModel.setCollection(sourceOntologyDto.getCollection());
                sourceOntologyObjectSetModel.setOntologyId(sourceOntologyDto.getOntologyId());
                sourceOntologyObjectSetModel.setUri(sourceOntologyDto.getUri());
                sourceOntologyObjectSetModel.setTitle(sourceOntologyDto.getTitle());

                sourceOntology.add(sourceOntologyObjectSetModel);

                log.info("sourceOntology.add(sourceOntologyObjectSetModel) Java heap memory: ");
                log.info("\t Free Memory \t Total Memory \t Max Memory");
                log.info("iteration: "+ iteration + ",  ontologies pair ( "+sourceOntologyDto.getOntologyId() +" , " +
                        ""+targetOntologyDto.getOntologyId() + " ): " +
                        "\t  " + Runtime.getRuntime().freeMemory() +
                        " \t " + Runtime.getRuntime().totalMemory() +
                        " \t " + Runtime.getRuntime().maxMemory());

                Set<OntologyDto> targetOntologySet = new HashSet<>();
                targetOntologySet.add(targetOntologyDto);

                log.info("targetOntologySet.add(targetOntology) Java heap memory: ");
                log.info("\t Free Memory \t Total Memory \t Max Memory");
                log.info("iteration: "+ iteration + ",  ontologies pair ( "+sourceOntologyDto.getOntologyId() +" , " +
                        ""+targetOntologyDto.getOntologyId() + " ): " +
                        "\t  " + Runtime.getRuntime().freeMemory() +
                        " \t " + Runtime.getRuntime().totalMemory() +
                        " \t " + Runtime.getRuntime().maxMemory());

                TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();
                targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);
                targetOntologyObjectSetModel.setNumberOfMappings(logmap2Mappings.size());
                targetOntologyObjectSetModel.setNumberOfConflictiveMappings(conflictiveLogmap2Mappings.size());

                targetOntologyObjectSetModel.setMappingList(getMappingList(logmap2Mappings));
                targetOntologyObjectSetModel.setConflictiveMappingsList(getMappingList(conflictiveLogmap2Mappings));

                targetOntologyObjectSetModelSet.add(targetOntologyObjectSetModel);

                log.info("targetOntologyObjectSetModelSet.add(targetOntologyObjectSetModel) Java heap memory: ");
                log.info("\t Free Memory \t Total Memory \t Max Memory");
                log.info("iteration: "+ iteration + ",  ontologies pair ( "+sourceOntologyDto.getOntologyId() +" , " +
                        ""+targetOntologyDto.getOntologyId() + " ): \t " + Runtime.getRuntime().freeMemory() +
                        " \t  " + Runtime.getRuntime().totalMemory() +
                        " \t  " + Runtime.getRuntime().maxMemory());

                targetOntologyList.add(targetOntologyObjectSetModel);
            }

                log.info("--the number of mappings between ("+ sourceOntologyDto.getOntologyId()+","+
                        targetOntologyDto.getOntologyId() +") ontologies is: " + logmap2Mappings.size());

                log.info("--the number of conflictive mappings between ("+ sourceOntologyDto.getOntologyId()+","+
                        targetOntologyDto.getOntologyId() +") ontologies is: " + conflictiveLogmap2Mappings.size());

                log.info("----- mapping between {} and {} ontologies is completed in {} ms",
                        sourceOntologyDto.getOntologyId(),
                        targetOntologyDto.getOntologyId(),
                        System.currentTimeMillis() - mappingForOneOntologyPairStartTime);

            }catch(Exception e){

            log.error("Mapping exception happened: " + e.getMessage());

            }

            iteration = iteration +1;
            
            }

            if(numberOfTargetOntologiesProcessed>0){

                ProcessedMapping processedMappingGroupedBySourceOntology =
                        preProcessingMappingService.preProcessGroupedBySourceOntology(sourceOntology, numberOfTargetOntologiesProcessed, targetOntologyObjectSetModelSet);

                processedMappingGroupedBySourceOntology.setId(sequenceGeneratorService.getSequenceNumber(ProcessedMapping.SEQUENCE_NAME));

                /**
                 * Save mappings to MongoDB
                 */
                processedMappingService.save(processedMappingGroupedBySourceOntology);

                log.info("processedMappingService.save(processedMappingGroupedBySourceOntology) Java heap memory: ");
                log.info("i \t Free Memory \t Total Memory \t Max Memory");
                log.info("iteration: "+ iteration + " \t " + Runtime.getRuntime().freeMemory() +
                        " \t \t " + Runtime.getRuntime().totalMemory() +
                        " \t \t " + Runtime.getRuntime().maxMemory());

                ExternalMapping externalMapping = processExternalMappingWithDtoSourceOntology(sourceOntologyDto,
                        numberOfTargetOntologiesProcessed, targetOntologyList);

                externalMappingList.add(externalMapping);
            }

        }

        log.info("---- all mappings are done in {} ms", System.currentTimeMillis() - mappingStartTime);
        log.info("Mappings are completed");

        return PageUtils.toPage(externalMappingList, pageable);

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

    private boolean existsOntologyInTerminologyService(String id, Collection<TsOntology> terminologyServiceOntologies){

        for(TsOntology ts: terminologyServiceOntologies){

            if(ts.getOntologyId().equals(id)){
                return true;
            }

            }

        return false;
    }

    /**
     * Produces mappings between a pair of two external ontologies (pairwise mappings)
     * @param sourceOntology
     * @param targetOntology
     * @param sat
     * @param pageable
     * @return
     * @param <T>
     *
     */
    @Override
    public <T extends ExtendedOntology> Page<ExternalMapping> getMappingsBetweenTwoExternalOntologies(T sourceOntology,
                                                                                                      T targetOntology,
                                                                                                      boolean sat,
                                                                                                      Pageable pageable
    )  {

        ProcessedOntology ont2 = ProcessedOntology.of(sourceOntology);

        List<ExternalMapping> externalMappingList = new ArrayList<>();

        Set<TargetOntologyObjectSetModel> targetOntologyList = new HashSet<TargetOntologyObjectSetModel>();

        ProcessedOntology ont1 = ProcessedOntology.of(targetOntology);

        int numberOfMappingsProcessed = 0;

        ontologyManager= OWLManager.createOWLOntologyManager();

        TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();

        /**
         * Returns empty mapping result when source and target ontologies have equal URIs
         */
        if(ont1.getUri().equals(ont2.getUri())){

            Exception e = new Exception();

            OntologyDto targetTSOntDto = OntologyDto.builder()
                    .ontologyId("")
                    .uri(ont1.getUri())
                    .title("")
                    .build();

            Set<OntologyDto> targetOntologySet = new HashSet<>();

            targetOntologySet.add(targetTSOntDto);

            /**
             * target ontology set
             */
            targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);

            targetOntologyObjectSetModel.setMappingException(getExeptionMessage(e, "ontologies " +
                    ont2.getUri().toString() +" and " + ont1.getUri() + " have equal URLs."));

            targetOntologyList.add(targetOntologyObjectSetModel);

            ExternalMapping externalMapping = processExternalMapping(ont2, 1, targetOntologyList);

            externalMappingList.add(externalMapping);

            return PageUtils.toPage(externalMappingList, pageable);

        };


        try {
            /**
             * generate first and second random numbers
             */
            Random r1 = SecureRandom.getInstanceStrong();
            Random r2 = SecureRandom.getInstanceStrong();

            long id = r1.nextLong() * r2.nextLong();
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
/**
 * Enable  HermiT reasoner during the computation of mappings. In Parameters class reasoning is set to HermiT.
 *
 */
            LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(
                    ontologyManager.loadOntology(IRI.create(
                            ont2.getUri())), ontologyManager.loadOntology(IRI.create(
                    ont1.getUri())), Parameters.hermit
            );;

            Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();
            Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

            if(sat) {

                targetOntologyObjectSetModel.setMappingException(getReasoningExplanation(logmap2Mappings, ont2, ont1 ));

            } else {

                targetOntologyObjectSetModel.setMappingException("Checking classes satisfiability is off");
            }

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

        }catch(Exception e){

            targetOntologyObjectSetModel.setMappingException(getExeptionMessage(e, " "));

            log.error("Mapping exception: " + e.getMessage());

        }

        targetOntologyList.add(targetOntologyObjectSetModel);

        numberOfMappingsProcessed++;

        ExternalMapping externalMapping = processExternalMapping(ont2, 1, targetOntologyList);

        externalMappingList.add(externalMapping);

        log.info("number of mappings processed: " + numberOfMappingsProcessed);

        return PageUtils.toPage(externalMappingList, pageable);

    }

    /**
     *  If checking classes unsatisfiability is
     *  selected as an option them short explanation is provided when
     *  checking unsatisfiability of merged mappings and input ontologies.
     *
     * @param logmap2Mappings
     * @param ont2
     * @param ont1
     * @return satisfiability of classes in merged mappings and input ontologies. Explanation is provided if exception
     * occurs,
     */
    private String getReasoningExplanation(Set<MappingObjectStr> logmap2Mappings, ProcessedOntology ont2, ProcessedOntology ont1 ){

        try{

            OWLOntologyManager ontoManager1;
            OWLOntologyManager ontoManager2;

            OWLOntology mappingsToOWLOntology = getOWLOntology4GivenMappings(logmap2Mappings);

            ontoManager1  = OWLManager.createOWLOntologyManager();
            ontoManager2  = OWLManager.createOWLOntologyManager();

            OWLOntology onto1 = ontoManager1.loadOntology(IRI.create(ont1.getUri()));
            OWLOntology onto2 = ontoManager2.loadOntology(IRI.create(ont2.getUri()));


                 /**
                  * Merge source ontology, target ontology and mappings ontology.
                  */
//                 OWLOntology mergedOntology = createMergedOntology(ontologyManager.
//                 loadOntology(IRI.create(ont2.getUri())),
//                         ontologyManager.loadOntology(IRI.create(ont1.getUri())),
//                         mappingsToOWLOntology);

            try {

                /**
                 * this classs is taken from LogMap matcher library (API)
                 */
                SatisfiabilityIntegration mappingsSatChecker = new SatisfiabilityIntegration(
                        onto2,
                        onto1,
                        mappingsToOWLOntology,//mappingsToOWLOntology , //mergedOntology,
                        true,//checks classes satisfiability
                        false,//Time_Out_Class
                        false); //use factory

                if (mappingsSatChecker.hasUnsatClasses()) {

                    log.info("merged "+ ont2.getUri() + " ontology, "+ ont1.getOntologyId()+" ontology and mappings ontology does not have unsatisfiable classes");

                    return "unsatisfiable classes: " + mappingsSatChecker.hasUnsatClasses() ;

                } else {

                    log.info("merged "+ ont2.getUri() + " ontology, "+ ont1.getOntologyId()+" ontology and mappings ontology does not have unsatisfiable classes");

                    return "unsatisfiable classes: " + mappingsSatChecker.hasUnsatClasses();

                }

            } catch(InconsistentOntologyException e){

            log.info("merged "+ ont2.getUri()+ " ontology, "+ ont1.getOntologyId()+" ontology and mappings ontology inconsistency: " + getExeptionMessage(e,""));

                return getExeptionMessage(e, " ");
            }

        }catch (OWLOntologyCreationException owlOntologyCreationException){

            log.info("owlOntologyCreationException.getLocalizedMessage(): " +  getExeptionMessage(owlOntologyCreationException,""));

            return getExeptionMessage(owlOntologyCreationException, "OWL ontology creation exception is detected:");

        } catch (Exception e) {

            String message =  new RuntimeException(e).getLocalizedMessage();

            log.info("runtime exception occurs: " + message);

            return getExeptionMessage(e," ");
        }
    }

    /**
     * Create a String object that contains explanation in case of throwing runtime exception or reasoning inconsistency
     * (un)satisfiability occurs.
     * @param e
     * @param message
     * @return
     */

    private String getExeptionMessage(Throwable e, String message) {

        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(e.getLocalizedMessage());

        return sb.toString();
    }

    /**
     * The method is borrowed from LogMap Matcher
     * @param mappings
     * @return
     * @throws Exception
     */
    private OWLOntology getOWLOntology4GivenMappings(Set<MappingObjectStr> mappings) throws Exception {

        OWLAlignmentFormat owlformat = new OWLAlignmentFormat("mappings.owl");

        for (MappingObjectStr mapping : mappings){

            if (mapping.getTypeOfMapping() == Utilities.INSTANCE){

                owlformat.addInstanceMapping2Output(
                        mapping.getIRIStrEnt1(),
                        mapping.getIRIStrEnt2(),
                        mapping.getConfidence());
            } else if (mapping.getTypeOfMapping() == Utilities.CLASSES){


                owlformat.addClassMapping2Output(
                        mapping.getIRIStrEnt1(),
                        mapping.getIRIStrEnt2(),
                        mapping.getMappingDirection(),
                        mapping.getConfidence());
            } else if (mapping.getTypeOfMapping() == Utilities.OBJECTPROPERTIES){

                owlformat.addObjPropMapping2Output(
                        mapping.getIRIStrEnt1(),
                        mapping.getIRIStrEnt2(),
                        mapping.getMappingDirection(),
                        mapping.getConfidence());
            }  else if (mapping.getTypeOfMapping() == Utilities.DATAPROPERTIES){

                owlformat.addDataPropMapping2Output(
                        mapping.getIRIStrEnt1(),
                        mapping.getIRIStrEnt2(),
                        mapping.getMappingDirection(),
                        mapping.getConfidence());
            }
        }//end for mappings

    return owlformat.getOWLOntology();
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

    private ExternalMapping processExternalMappingWithDtoSourceOntology(OntologyDto ont1,
                                                   int numberOfTargetOntologies,
                                                   Set<TargetOntologyObjectSetModel> targetOntologyList) {
        return ExternalMapping.builder()
                .mappingId(UUID.randomUUID().toString())
                .sourceOntologyURI(ont1.getUri())
                .numberOfTargetOntologies(numberOfTargetOntologies)
                .targetOntologyList(targetOntologyList)
                .build();
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

    private ExternalMapping processExternalMultifilePartMapping(OWLOntology ont1,
                                                   int numberOfTargetOntologies,
                                                   Set<TargetOntologyObjectSetModel> targetOntologyList) {
        return ExternalMapping.builder()
                .mappingId(UUID.randomUUID().toString())
                .sourceOntologyURI(ont1.getOntologyID().getOntologyIRI().get().toURI().toString())
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