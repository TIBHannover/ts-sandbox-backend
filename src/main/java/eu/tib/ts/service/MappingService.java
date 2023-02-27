package eu.tib.ts.service;

import eu.tib.ts.model.ontology.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MappingService {

    Page<Mapping> getMappings(List<String> ids,
                                     CharacteristicsType characteristicsType,
                                     Optional<String> collection,
                                     Pageable pageable);

    Page<Mapping> getMappings(String id,
                                     CharacteristicsType characteristicsType,
                                     Optional<String> collection,
                                     Pageable pageable);

    <T extends ExtendedOntology> Page<Mapping> getMappings(T ontology,
                                                                  CharacteristicsType characteristicsType,
                                                                  Optional<String> collection,
                                                                  Pageable pageable);

    Page<PairwiseMapping> getPairwiseMapping(String id,
                                                   Optional<List<String>> ids,
                                                   Optional<String> collection,
                                                   Pageable pageable);

    Page<PairwiseMapping> getPairwiseMapping(Optional<List<String>> ids,
                                                   Optional<String> collection,
                                                   Pageable pageable);

    Page<PairwiseMapping> getPairwiseMapping(String id,
                                                   Optional<String> collection,
                                                   Pageable pageable);

    <T extends ExtendedOntology> Page<PairwiseMapping> getPairwiseMapping(T ontology,
                                                                                Optional<String> collection,
                                                                                Pageable pageable);

    <T extends ExtendedOntology> Page<PairwiseMapping> getPairwiseMapping(T ontology,
                                                                                Optional<List<String>> ids,
                                                                                Optional<String> collection,
                                                                                Pageable pageable);
}


