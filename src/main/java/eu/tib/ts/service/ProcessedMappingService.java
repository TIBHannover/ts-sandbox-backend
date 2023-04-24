package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.model.ontology.ProcessedMapping;

import java.util.List;

public interface ProcessedMappingService {

public List<ProcessedMapping> findAll();
public List<MappingDto> getAllMappings();

ProcessedMapping save (ProcessedMapping processedMapping);

}
