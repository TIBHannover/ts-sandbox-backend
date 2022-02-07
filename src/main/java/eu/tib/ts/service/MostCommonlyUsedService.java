package eu.tib.ts.service;

import eu.tib.ts.controller.dto.KeyValueResultDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MostCommonlyUsedService {
    PageImpl<KeyValueResultDto> getMostCommonlyUsedCharacteristics(Optional<List<String>> ids,
                                                                   CharacteristicsType characteristicsType,
                                                                   Optional<String> collection,
                                                                   Pageable pageable);
}
