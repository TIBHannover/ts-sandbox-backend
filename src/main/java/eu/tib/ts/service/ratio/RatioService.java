package eu.tib.ts.service.ratio;

import eu.tib.ts.model.ontology.Ontology;

import java.util.List;

public interface RatioService {
    <T extends Ontology> double getRatio(List<T> ontologies);
}
