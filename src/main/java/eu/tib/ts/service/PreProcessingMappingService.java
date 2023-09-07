package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.controller.dto.TargetOntologyListObjectSetModel;
import eu.tib.ts.model.ontology.ProcessedMapping;

import java.util.Set;

public interface PreProcessingMappingService {

    public ProcessedMapping preProcess(Set<OntologyDto> sourceOntologySet, Set<OntologyDto> targetOntologySet, int numberOfMappings,
                                       int numberOfConflictiveMappings, Set<MappingObjectSetModel> mappingList,
                                       Set<MappingObjectSetModel> conflictiveMappingsList);

    public ProcessedMapping preProcessGroupedBySourceOntology(Set<OntologyDto> sourceOntologySet, int numberOfTargetOntologies,
                                                              Set<TargetOntologyListObjectSetModel> targetOntologyList);


}
