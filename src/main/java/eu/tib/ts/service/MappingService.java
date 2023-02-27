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
}


