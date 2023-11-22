package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedMongoOntologyRepository;
import eu.tib.ts.service.MappingFilterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MappingFilterServiceImpl implements MappingFilterService {

    /**
     * @author Nenad Krdzavac
     *
     * Filters list of mappings if collection is present, otherwise does not present any mappings.
     * Based on selected one or more collection mappings are filltered for source ontologies
     * that belong to selected collections.
     *
     * @param processedMappings
     * @param collection
     * @return
     */
    @Override
    public List<ProcessedMapping> filterMappings(List<ProcessedMapping> processedMappings, Optional<String> collection) {

        return collection
                .map(s -> processedMappings.stream()
                        .filter(processedMapping -> processedMapping.getCollection().stream()
                                .anyMatch(s::equalsIgnoreCase))
                        .collect(Collectors.toList()))
                .orElse(processedMappings);
    }

}
