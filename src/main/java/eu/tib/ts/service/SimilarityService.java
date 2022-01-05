package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.Similarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SimilarityService {
    <T extends Ontology> Page<Similarity> getSharedPropertyUri(List<T> ontologies, Pageable pageable);

    <T extends ExtendedOntology> Page<Similarity> getSharedPropertyUri(T ontology, Pageable pageable);

    <T extends Ontology> Page<Similarity> getSharedClassUri(List<T> ontologies, Pageable pageable);
}
