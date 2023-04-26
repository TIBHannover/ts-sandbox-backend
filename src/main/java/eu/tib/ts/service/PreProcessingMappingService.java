package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.model.ontology.ProcessedMapping;

import java.util.Set;

public interface PreProcessingMappingService {

    public ProcessedMapping preProcess(String sourceOntology, String targetOntology, int numberOfMappings,
                                       int numberOfConflictiveMappings, Set<MappingObjectSetModel> mappingList,
                                       Set<MappingObjectSetModel> conflictiveMappingsList);
}
