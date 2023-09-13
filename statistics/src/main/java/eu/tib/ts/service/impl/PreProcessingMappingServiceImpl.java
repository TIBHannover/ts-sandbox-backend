package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.controller.dto.TargetOntologyObjectSetModel;
import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.service.PreProcessingMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class PreProcessingMappingServiceImpl implements PreProcessingMappingService {


    @Override
    public ProcessedMapping preProcess(Set<OntologyDto> sourceOntology, Set<OntologyDto> targetOntology, int numberOfMappings,
                                       int numberOfConflictiveMappings, Set<MappingObjectSetModel> mappingList,
                                       Set<MappingObjectSetModel> conflictiveMappingsList) {

        log.info("start pre-processing mapping between ontologies : " );

        /**
         * generated random uuid is assigned to mapping id
         */
        final String uuid = UUID.randomUUID().toString().replace("-", "");

        return ProcessedMapping.builder()
                .mappingId(uuid)
                .numberOfMappings(numberOfMappings)
                .numberOfConflictiveMappings(numberOfConflictiveMappings)
                .sourceOntology(sourceOntology)
                .targetOntology(targetOntology)
                .mappingList(mappingList)
                .conflictiveMappingsList(conflictiveMappingsList)
                .build();
    }

    @Override
    public ProcessedMapping preProcessGroupedBySourceOntology(Set<OntologyDto> sourceOntologySet, int numberOfTargetOntologies, Set<TargetOntologyObjectSetModel> targetOntologyList) {

        log.info("start pre-processing mapping between ontologies grouped by source ontology: " );

        /**
         * generated random uuid is assigned to mapping id
         */
        final String uuid = UUID.randomUUID().toString().replace("-", "");

        return ProcessedMapping.builder()
                .mappingId(uuid)
                .sourceOntologySet(sourceOntologySet)
                .numberOfTargetOntologies(numberOfTargetOntologies)
                .targetOntologyList(targetOntologyList)
                .build();



    }
}