package eu.tib.ts.service.impl;

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

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
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
    public <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology, Optional<List<String>> ids, Optional<String> collection, Pageable pageable) throws OWLOntologyCreationException {

        List<ProcessedOntology> processedOntologies = ids.isPresent()
                ? getProcessedOntologies(ids.get())
                : getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        List<ProcessedOntology> filteredTSOntologies = ontologyFilterService.filter(processedOntologies, collection);

        ProcessedOntology ont2 = ProcessedOntology.of(ontology);

        List<ExternalMapping> externalMappingList = new ArrayList<>();

        Set<OntologyPair> set = new HashSet<>();

        int numberOfTargetOntologies = filteredTSOntologies.size();

        Set<TargetOntologyObjectSetModel> targetOntologyList = new HashSet<TargetOntologyObjectSetModel>();

        for (ProcessedOntology ont1 : filteredTSOntologies) {

            ontologyManager= OWLManager.createOWLOntologyManager();

            OntologyPair pair = OntologyPair.of(ont1, ont2);

            if (ont1.equalsTsOntology(ont2) || set.contains(pair.inverted())) {
                continue;
            }

            set.add(pair);

            try {

            TargetOntologyObjectSetModel targetOntologyObjectSetModel = new TargetOntologyObjectSetModel();

            LogMap2_Matcher logmap2GroupedBySourceOntology = new LogMap2_Matcher(ontologyManager.loadOntology(IRI.create(
                        ont2.getUri())), ontologyManager.loadOntology(IRI.create(
                        ont1.getUri())));

            Set<MappingObjectStr> logmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_Mappings();

            Set<MappingObjectStr>  conflictiveLogmap2Mappings = logmap2GroupedBySourceOntology.getLogmap2_ConflictiveMappings();

                /**
                 * generate first random number
                 */
                Random r_1 = SecureRandom.getInstanceStrong();
                Random r_2 = SecureRandom.getInstanceStrong();
                long id = r_1.nextLong()*r_2.nextLong();

                targetOntologyObjectSetModel.setId(id);
                /**
                 * target ontology data
                 */
                Set<OntologyDto> targetOntologySet = new HashSet<>();

                /**
                 * target ontology dto from terminology service
                 */
                OntologyDto targetTSOntDto = OntologyDto.builder()
                        .ontologyId(ont1.getOntologyId())
                        .uri(ont1.getUri())
                        .title(ont1.getTitle())
                        .build();
                targetOntologySet.add(targetTSOntDto);
                targetOntologyObjectSetModel.setTargetOntology(targetOntologySet);

                /**
                 * Store mappings information in target ontology object set model
                 */
                for(MappingObjectStr mappingObjectStr: logmap2Mappings){

                log.info(mappingObjectStr.getIRIStrEnt1() + " " + mappingObjectStr.getIRIStrEnt2() + " "
                        + mappingObjectStr.getMappingDirection() + " " + mappingObjectStr.getTypeOfMapping()
                        + " " + mappingObjectStr.getConfidence() + " " + mappingObjectStr.getStructuralConfidenceMapping());









            }

            ExternalMapping externalMapping = processExternalMapping(ont2, numberOfTargetOntologies, targetOntologyList);

            externalMappingList.add(externalMapping);

            }catch(Exception e){

                log.error("Exception happened: " + e.getMessage());

            }
        }

    return PageUtils.toPage(externalMappingList, pageable);

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
