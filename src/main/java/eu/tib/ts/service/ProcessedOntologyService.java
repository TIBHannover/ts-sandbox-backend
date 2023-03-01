package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ProcessedOntology;

import java.util.List;
import java.util.Optional;

public interface ProcessedOntologyService {
    List<ProcessedOntology> findAll();

    List<OntologyDto> getOntologies();

    List<String> getOntologyIds();

    ProcessedOntology save(ProcessedOntology processedOntology);
}
