package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.service.PreProcessingMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class PreProcessingMappingServiceImpl implements PreProcessingMappingService {

    public static final String EXTERNAL = "external";

    long idd = 0;


    @Override
    public ProcessedMapping preProcess(String sourceOntology, String targetOntology, int numberOfMappings, Set<MappingObjectSetModel> mappingList) {

        log.info("start pre-processing mapping between ontologies : " );

        final String uuid = UUID.randomUUID().toString().replace("-", "");

        return ProcessedMapping.builder()
                .mappingId(uuid)
                .numberOfMappings(numberOfMappings)
                .sourceOntology(sourceOntology)
                .targetOntology(targetOntology)
                .mappingList(mappingList)
                .build();
    }
}