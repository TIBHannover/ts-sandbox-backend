package eu.tib.ts.service;

import eu.tib.ts.controller.dto.RatioDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.Ontology;

import java.util.List;

public interface RatioService {
    <T extends Ontology> RatioDto getRatio(List<T> ontologies, CharacteristicsType characteristicsType);
}
