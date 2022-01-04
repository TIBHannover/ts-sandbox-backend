package eu.tib.ts.service;

import eu.tib.ts.controller.dto.SharedClassUriDto;
import eu.tib.ts.controller.dto.SharedPropertyUriDto;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.Ontology;

import java.util.List;

public interface SimilarityService {
    <T extends Ontology> SharedPropertyUriDto getSharedPropertyUri(List<T> ontologies);

    <T extends ExtendedOntology> SharedPropertyUriDto getSharedPropertyUri(T ontology);

    SharedClassUriDto getSharedClassUri(List<? extends Ontology> ontologies);
}
