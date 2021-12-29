package eu.tib.ts.repository;

import eu.tib.ts.model.ontology.TsOntology;

import java.util.List;

public interface TsRepository {
    List<TsOntology> getOntologies();
}
