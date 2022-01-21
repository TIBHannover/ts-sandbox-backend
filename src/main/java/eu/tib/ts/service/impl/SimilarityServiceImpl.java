package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SimilarityServiceImpl implements SimilarityService {
    private final ProcessedOntologyRepository processedOntologyRepository;
    private final OntologyFilterService filterService;

    @Autowired
    protected SimilarityServiceImpl(ProcessedOntologyRepository processedOntologyRepository,
                                    OntologyFilterService filterService) {
        this.processedOntologyRepository = processedOntologyRepository;
        this.filterService = filterService;
    }

    public Page<Similarity> getSimilarities(List<String> ids,
                                            CharacteristicsType characteristicsType,
                                            Optional<String> collection,
                                            Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies(ids);
        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }
        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);

        List<Pair<String, ProcessedOntology>> pairs = getCharacteristicsPairs(filteredOntologies, characteristicsType);
        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
        List<Similarity> list = getSimilarityList(map, false);

        return PageUtils.toPage(list, pageable);
    }

    public <T extends ExtendedOntology> Page<Similarity> getSimilarities(T ontology,
                                                                         CharacteristicsType characteristicsType,
                                                                         Optional<String> collection,
                                                                         Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);

        ProcessedOntology externalOntology = ProcessedOntology.of(ontology);
        filteredOntologies.add(externalOntology);

        List<Pair<String, ProcessedOntology>> pairs =
            getCharacteristicsPairs(processedOntologies, ontology, characteristicsType);
        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
        List<Similarity> list = getSimilarityList(map, true);

        return PageUtils.toPage(list, pageable);
    }

    private List<Pair<String, ProcessedOntology>> getCharacteristicsPairs(
        List<ProcessedOntology> processedOntologies,
        CharacteristicsType characteristicsType
    ) {
        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String item : characteristicsType.getCharacteristics(processedOntology)) {
                pairs.add(Pair.of(item, processedOntology));
            }
        }

        return pairs;
    }

    private <T extends ExtendedOntology> List<Pair<String, ProcessedOntology>> getCharacteristicsPairs(
        List<ProcessedOntology> processedOntologies,
        T ontology,
        CharacteristicsType characteristicsType
    ) {
        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String item : characteristicsType.getCharacteristics(processedOntology)) {
                if (characteristicsType.getCharacteristics(ontology).contains(item)
                    && !processedOntology.equalsTsOntology(ontology)) {
                    pairs.add(Pair.of(item, processedOntology));
                }
            }
        }

        return pairs;
    }

    private List<Similarity> getSimilarityList(Map<String, List<OntologyDto>> map, boolean external) {
        return map.entrySet().stream()
            .filter(entry -> external || entry.getValue().size() > 1)
            .map(entry -> Similarity.builder()
                .name(entry.getKey())
                .ontologies(entry.getValue())
                .build()
            )
            .collect(Collectors.toList());
    }

    private List<ProcessedOntology> getProcessedOntologies(List<String> ids) {

        return processedOntologyRepository.findByOntologyIdIn(ids);
    }

    private List<ProcessedOntology> getProcessedOntologies() {

        return StreamSupport.stream(processedOntologyRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    }

    private Map<String, List<OntologyDto>> getSimilarityMap(List<Pair<String, ProcessedOntology>> pairs) {
        Map<String, List<OntologyDto>> similarityMap = pairs.stream()
            .collect(
                Collectors.groupingBy(
                    Pair::getFirst,
                    Collectors.mapping(pair -> OntologyDto.of(pair.getSecond()),
                        Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> list.stream()
                                .sorted(Comparator.comparing(OntologyDto::getOntologyId))
                                .collect(Collectors.toList())
                        )
                    )
                )
            );

        return similarityMap.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new
                )
            );
    }
}
