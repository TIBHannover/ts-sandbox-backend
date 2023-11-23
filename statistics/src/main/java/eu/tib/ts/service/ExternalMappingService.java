package eu.tib.ts.service;

import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ExternalMappingService {

    <T extends ExtendedOntology> Page<ExternalMapping> getPairwiseSimilarity(T ontology,
                                                                             Optional<String> collection,
                                                                             Pageable pageable);

}
