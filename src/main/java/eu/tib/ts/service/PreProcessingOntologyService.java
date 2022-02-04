package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;

import java.util.Optional;

public interface PreProcessingOntologyService {
    ProcessedOntology preProcess(Optional<TsOntology> tsOntology, String fileLocation);
}
