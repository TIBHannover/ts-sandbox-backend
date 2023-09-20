package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;
import eu.tib.ts.model.ontology.ProcessedMapping;

import java.util.List;

/**
 * Service that calculates mappings between a pair of ontologies, and save mappings to MongoDB
 */
public interface ProcessedMappingService {

//public List<MappingDto> getAllMappings();

public List<MappingGropedBySourceOntologyDto> getAllMappingsGroupedBySourceOntology();

ProcessedMapping save (ProcessedMapping processedMapping);

}
