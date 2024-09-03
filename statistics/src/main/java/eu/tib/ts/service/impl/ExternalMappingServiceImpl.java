package eu.tib.ts.service.impl;

import eu.tib.ts.configuration.OntologiesProcessingConfig;
import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.controller.dto.SourceOntologyObjectSetModel;
import eu.tib.ts.controller.dto.TargetOntologyObjectSetModel;
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

    private boolean ontologyExists(TsOntology tsOntology, List<ProcessedOntology> processedOntologies) {
        return processedOntologies.stream()
                .anyMatch(ont -> ont.equalsTsOntology(tsOntology));
    }

    /**
     *
     * @param ids
     * @param pageable
     * @return
     * @param <T>
     */
    @Override
    public <T extends ExtendedOntology> Page<ExternalMapping> getAllTIBTSOntologiesAndProcessedOntologiesInMappings(
            List<String> ids,
            Pageable pageable) {

        Collection<TsOntology> terminologyServiceOntologies = tsRepository.getOntologies();
        List<ProcessedOntology> processedOntologies = processedOntologyService.findAll();

        Set<OntologyDto> newOntologySetFromParameterList = new HashSet<>();

        /**
         * creates a list of source ontologies from ontology ids in parameter list by comparing IDs with
         * TIB TS ontology IDs
         */
        for(String id: ids){

            /**
             * checks if ontology id exists in TIB TS and does not exists in processed
             * ontology list (ontologies for which mappings are computted)
             */
            if(!(existsOntologyInProcessedOntologyList(id, processedOntologies)) &&
                    existsOntologyInTerminologyService(id, terminologyServiceOntologies)){

            for(TsOntology terminologyServiceOntology: terminologyServiceOntologies){

                    OntologyDto sourceTsOntDto = OntologyDto.builder()
                            .ontologyId(terminologyServiceOntology.getOntologyId())
                            .uri(terminologyServiceOntology.getUri())
                            .title(terminologyServiceOntology.getTitle())
                            .collection(terminologyServiceOntology.getCollection())
                            .build();

                    newOntologySetFromParameterList.add(sourceTsOntDto);
                }
            }
        }

        log.info("tsOnt.size(): " + terminologyServiceOntologies.size());
        log.info("TIB Terminology Service ontologies:");
        int i = 1;

        for (TsOntology ts : terminologyServiceOntologies) {

            log.info(i++ + ". ont id: " + ts.getOntologyId() + " , ont uri: " + ts.getUri());

        }

        log.info("list of ontology ids from parameter list:");
        for (String id : ids) {

            for (TsOntology ts : terminologyServiceOntologies) {

                if (ts.getOntologyId().equals(id)) {

                    log.info("tib ts ontology id: " + ts.getOntologyId() + ", parameter id: " + id + ", ontology title: " + ts.getTitle());

                }

            }
        }

        log.info("Processed ontologies in MongoDB: ");
        int p = 1;

        for (ProcessedOntology pso : processedOntologies) {

            log.info("ontology-id: " + pso.getId() + " , " + p++ + ". " + pso.getOntologyId() + " ,  uri: " + pso.getUri());

        }

        log.info("Check membership of ontologies from parameter list in processed ontologies and TIB TS: ");
        for (String id : ids) {

            log.info("ontology id from parameter list: " + id);

            if (!(existsOntologyInProcessedOntologyList(id, processedOntologies)) &&
                    existsOntologyInTerminologyService(id, terminologyServiceOntologies)) {

            log.info("---ontology " + id + " does exist in processed ontologies (Mongo DB) and exists in TIB TS");

            Set<TargetOntologyObjectSetModel> targetOntologyList = new HashSet<TargetOntologyObjectSetModel>();

            log.info("Compute mappings between" + id +" ontology and processed ontologies from Mongo DB:");
            for (ProcessedOntology pso : processedOntologies) {

                log.info(" target ontology id: " + pso.getId()+ " target ontology title: " + pso.getTitle() +
                        " target ontology url: " + pso.getUri());



            }

            }
        }

        List<ExternalMapping> externalMappingList = new ArrayList<>();
        return PageUtils.toPage(externalMappingList, pageable);

    }

    /**
     *
     * @param id
     * @param processedOntologies
     * @return
     */
    private boolean existsOntologyInProcessedOntologyList(String id, List<ProcessedOntology> processedOntologies){

    for(ProcessedOntology pso: processedOntologies){

    if(pso.getOntologyId().equals(id)) {

        return true;
    }

    }

    return false;

    }

    private boolean existsOntologyInTerminologyService(String id, Collection<TsOntology> terminologyServiceOntologies){

        for(TsOntology ts: terminologyServiceOntologies){

            if(ts.getOntologyId().equals(id)){


                log.info("---matched ontologies: ");
                log.info("-----------tib ts ontology id: " + ts.getOntologyId() + ", parameter id: " + id + ", ontology title: " + ts.getTitle());

                return true;
            }

            }

        return false;
    }

    /**
     * Return true if ontology id belongs to processed ontology list, otherwise retuns false
     * @param ontologyid
     * @param processedOntology
     * @return
     */
    public boolean existsOntologyId(String ontologyid, List<ProcessedOntology> processedOntology){

       int n=0;

       for(ProcessedOntology pso: processedOntology){

       if(pso.getOntologyId().equals(ontologyid)) {

           return true;
       }

       }

     return false;

    }

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
//                 OWLOntology mergedOntology = createMergedOntology(ontologyManager.loadOntology(IRI.create(ont2.getUri())),
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