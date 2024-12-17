package eu.tib.ts.assessments.repository;



import eu.tib.ts.assessments.model.tags.ontology.TsOntology;

import java.util.List;

public interface TsRepository {
    List<TsOntology> getOntologies();
}
