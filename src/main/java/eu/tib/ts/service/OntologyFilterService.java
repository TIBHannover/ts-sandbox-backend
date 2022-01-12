package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedOntology;

import java.util.List;
import java.util.Optional;

public interface OntologyFilterService {
    List<ProcessedOntology> filter(List<ProcessedOntology> processedOntologies, Optional<String> collection);
}
