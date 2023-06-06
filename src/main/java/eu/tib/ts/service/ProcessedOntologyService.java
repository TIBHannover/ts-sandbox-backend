package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ProcessedOntology;

import java.util.List;
import java.util.Optional;

public interface ProcessedOntologyService {

    List<ProcessedOntology> findAll();

    /**
     * author nenad.krdzavac@tib.eu
     * added: 01.03.2023.
     * list of all ontologies including collections they belong to
     * @return
     */
    List<OntologyDto> getMappingOntologies();

    List<OntologyDto> getOntologies();

    List<String> getOntologyIds();

    ProcessedOntology save(ProcessedOntology processedOntology);

}