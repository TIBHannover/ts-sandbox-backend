package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedOntology;

import java.util.List;

public interface ProcessedOntologyService {
    List<ProcessedOntology> findAll();

    ProcessedOntology save(ProcessedOntology processedOntology);
}
