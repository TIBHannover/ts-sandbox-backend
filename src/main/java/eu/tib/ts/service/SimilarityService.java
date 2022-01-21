package eu.tib.ts.service;

import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.Similarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SimilarityService {
    Page<Similarity> getSimilarities(List<String> ids,
                                     CharacteristicsType characteristicsType,
                                     Optional<String> collection,
                                     Pageable pageable);

    <T extends ExtendedOntology> Page<Similarity> getSimilarities(T ontology,
                                                                  CharacteristicsType characteristicsType,
                                                                  Optional<String> collection,
                                                                  Pageable pageable);
}
