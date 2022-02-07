package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.KeyValueResultDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.MostCommonlyUsedService;
import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MostCommonlyUsedServiceImpl implements MostCommonlyUsedService {
    private final ProcessedOntologyRepository processedOntologyRepository;
    private final OntologyFilterService filterService;

    private static final Set<String> NAMESPACES_EXCLUSION_SET = Set.of(
        "https://w3id.org/mdo/full/",
        "http://www.w3.org/2002/07/owl",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns",
        "http://www.w3.org/xml/1998/namespace",
        "http://www.w3.org/2001/xmlschema",
        "http://www.w3.org/2000/01/rdf-schema"
    );

    @Autowired
    public MostCommonlyUsedServiceImpl(ProcessedOntologyRepository processedOntologyRepository,
                                       OntologyFilterService filterService) {
        this.processedOntologyRepository = processedOntologyRepository;
        this.filterService = filterService;
    }

    @Override
    public PageImpl<KeyValueResultDto> getMostCommonlyUsedCharacteristics(Optional<List<String>> ids,
                                                                          CharacteristicsType characteristicsType,
                                                                          Optional<String> collection,
                                                                          Pageable pageable) {
        List<ProcessedOntology> processedOntologies = ids.isPresent()
            ? getProcessedOntologies(ids.get())
            : getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }
        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);

        List<String> characteristics = new ArrayList<>();
        filteredOntologies.forEach(processedOntology -> {
            for (String characteristic : characteristicsType.getCharacteristics(processedOntology)) {
                if (CharacteristicsType.NAMESPACE.equals(characteristicsType) &&
                    NAMESPACES_EXCLUSION_SET.stream().anyMatch(characteristic::startsWith)) {
                    continue;
                }
                characteristics.add(characteristic);
            }
        });

        Map<String, Long> map = characteristics.stream()
            .collect(
                Collectors.groupingBy(
                    s -> s,
                    Collectors.counting()
                )
            )
            .entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new
                )
            );

        List<KeyValueResultDto> list = map.entrySet().stream()
            .map(entry -> KeyValueResultDto.builder().key(entry.getKey()).value(entry.getValue()).build())
            .collect(Collectors.toList());

        return PageUtils.toPage(list, pageable);
    }

    private List<ProcessedOntology> getProcessedOntologies(List<String> ids) {

        return processedOntologyRepository.findByOntologyIdIn(ids);
    }

    private List<ProcessedOntology> getProcessedOntologies() {

        return StreamSupport.stream(processedOntologyRepository.findAll().spliterator(), false)
            .sorted(Comparator.comparing(ProcessedOntology::getOntologyId))
            .collect(Collectors.toList());
    }
}
