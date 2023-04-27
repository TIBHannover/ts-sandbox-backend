package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
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
    public ProcessedMapping preProcess(String sourceOntology, String targetOntology, int numberOfMappings,
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
}