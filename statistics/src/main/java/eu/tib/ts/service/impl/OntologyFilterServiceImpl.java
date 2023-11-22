package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.service.OntologyFilterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OntologyFilterServiceImpl implements OntologyFilterService {

    /**
     * Filters list of ontologies if collection is present, otherwise does not
     *
     * @param processedOntologies list of ontologies to be filtered
     * @param collection          optional collection value
     * @return list of ontologies
     */
    @Override
    public List<ProcessedOntology> filter(List<ProcessedOntology> processedOntologies, Optional<String> collection) {
        return collection
            .map(s ->
                processedOntologies.stream()
                    .filter(processedOntology -> processedOntology.getCollection().stream()
                        .anyMatch(s::equalsIgnoreCase))
                        .collect(Collectors.toList()))
            .orElse(processedOntologies);
    }
}