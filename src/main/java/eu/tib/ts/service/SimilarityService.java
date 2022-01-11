package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.Similarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SimilarityService {
    <T extends Ontology> Page<Similarity> getSimilarities(List<T> ontologies, Pageable pageable);

    <T extends ExtendedOntology> Page<Similarity> getSimilarities(T ontology, Pageable pageable);
}
