package eu.tib.ts.service;

import eu.tib.ts.model.ontology.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MappingService {

    Page<Mapping> getMappings(List<String> ids,
                                     CharacteristicsType characteristicsType,
                                     Optional<String> collection,
                                     Pageable pageable);

    Page<PairwiseMapping> getPiarwiseMapping(Optional<List<String>> ids, Optional<String> collection, Pageable pageable);



}


