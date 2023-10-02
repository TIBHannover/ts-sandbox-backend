package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface MappingFilterService {

    List<ProcessedMapping> filterMappings(List<ProcessedMapping> processedMappings, Optional<String> collection);
}
