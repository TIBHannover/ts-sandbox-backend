package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.CharacteristicsInfo;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.OntologyPair;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class SimilarityServiceImpl implements SimilarityService {
    private final ProcessedOntologyRepository processedOntologyRepository;
    private final OntologyFilterService filterService;
    private final SimilaritySettings settings;

    @Autowired
    protected SimilarityServiceImpl(ProcessedOntologyRepository processedOntologyRepository,
                                    OntologyFilterService filterService,
                                    SimilaritySettings similaritySettings) {
        this.processedOntologyRepository = processedOntologyRepository;
        this.filterService = filterService;
        this.settings = similaritySettings;
    }

    @Override
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

    @Override
    public Page<Similarity> getSimilarities(List<String> ids,
                                            CharacteristicsType characteristicsType,
                                            Optional<String> collection,
                                            String id,
                                            Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies(ids);
        Optional<ProcessedOntology> givenOntology = getProcessedOntologies(Collections.singletonList(id)).stream()
            .findFirst();

        if (CollectionUtils.isEmpty(processedOntologies) || givenOntology.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }
        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);
        filteredOntologies.add(givenOntology.get());

        List<Pair<String, ProcessedOntology>> pairs =
            getCharacteristicsPairs(filteredOntologies, givenOntology.get(), characteristicsType);
        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
        List<Similarity> list = getSimilarityList(map, true);

        return PageUtils.toPage(list, pageable);
    }

    @Override
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
            getCharacteristicsPairs(filteredOntologies, ontology, characteristicsType);
        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
        List<Similarity> list = getSimilarityList(map, true);

        return PageUtils.toPage(list, pageable);
    }

    @Override
    public Page<PairwiseSimilarity> getPairwiseSimilarity(Optional<List<String>> ids,
                                                          Optional<String> collection,
                                                          Pageable pageable) {
        List<ProcessedOntology> processedOntologies = ids.isPresent()
            ? getProcessedOntologies(ids.get())
            : getProcessedOntologies();
        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }
        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);

        List<PairwiseSimilarity> pairwiseSimilarities = new ArrayList<>();
        Set<OntologyPair> set = new HashSet<>();
        for (ProcessedOntology ont1 : filteredOntologies) {
            for (ProcessedOntology ont2 : filteredOntologies) {
                OntologyPair pair = OntologyPair.of(ont1, ont2);
                if (ont1.equalsTsOntology(ont2) || set.contains(pair.inverted())) {
                    continue;
                }
                set.add(pair);
                Triple<Double, Double, Map<String, CharacteristicsInfo>> triple = processPairs(ont1, ont2);
                pairwiseSimilarities.add(
                    buildPairwiseSimilarities(ont1, ont2, triple.getRight(), triple.getLeft(), triple.getMiddle())
                );
            }
        }

        List<PairwiseSimilarity> sorted = pairwiseSimilarities.stream()
            .filter(aggregatedSimilarity -> aggregatedSimilarity.getSum() > 0)
            .sorted(Comparator.comparing(PairwiseSimilarity::getPercent).reversed())
            .collect(Collectors.toList());

        return PageUtils.toPage(sorted, pageable);
    }

    @Override
    public Page<PairwiseSimilarity> getPairwiseSimilarity(Optional<List<String>> ids,
                                                          Optional<String> collection,
                                                          String id,
                                                          Pageable pageable) {
        List<ProcessedOntology> processedOntologies = ids.isPresent()
            ? getProcessedOntologies(ids.get())
            : getProcessedOntologies();

        Optional<ProcessedOntology> givenOntology = getProcessedOntologies(Collections.singletonList(id)).stream()
            .findFirst();

        if (CollectionUtils.isEmpty(processedOntologies) || givenOntology.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }
        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);
        ProcessedOntology ont1 = ProcessedOntology.of(givenOntology.get());

        List<PairwiseSimilarity> pairwiseSimilarities = new ArrayList<>();
        Set<OntologyPair> set = new HashSet<>();
        for (ProcessedOntology ont2 : filteredOntologies) {
            OntologyPair pair = OntologyPair.of(ont1, ont2);
            if (ont1.equalsTsOntology(ont2) || set.contains(pair.inverted())) {
                continue;
            }
            set.add(pair);
            Triple<Double, Double, Map<String, CharacteristicsInfo>> triple = processPairs(ont1, ont2);
            pairwiseSimilarities.add(
                buildPairwiseSimilarities(ont1, ont2, triple.getRight(), triple.getLeft(), triple.getMiddle())
            );
        }

        List<PairwiseSimilarity> sorted = pairwiseSimilarities.stream()
            .filter(aggregatedSimilarity -> aggregatedSimilarity.getSum() > 0)
            .sorted(Comparator.comparing(PairwiseSimilarity::getPercent).reversed())
            .collect(Collectors.toList());

        return PageUtils.toPage(sorted, pageable);
    }

    @Override
    public <T extends ExtendedOntology> Page<PairwiseSimilarity> getPairwiseSimilarity(T ontology,
                                                                                       Optional<String> collection,
                                                                                       Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }
        List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);
        ProcessedOntology ont2 = ProcessedOntology.of(ontology);

        List<PairwiseSimilarity> pairwiseSimilarities = new ArrayList<>();
        Set<OntologyPair> set = new HashSet<>();
        for (ProcessedOntology ont1 : filteredOntologies) {
            OntologyPair pair = OntologyPair.of(ont1, ont2);
            if (ont1.equalsTsOntology(ont2) || set.contains(pair.inverted())) {
                continue;
            }
            set.add(pair);
            Triple<Double, Double, Map<String, CharacteristicsInfo>> triple = processPairs(ont1, ont2);
            pairwiseSimilarities.add(
                buildPairwiseSimilarities(ont1, ont2, triple.getRight(), triple.getLeft(), triple.getMiddle())
            );
        }

        List<PairwiseSimilarity> sorted = pairwiseSimilarities.stream()
            .filter(aggregatedSimilarity -> aggregatedSimilarity.getSum() > 0)
            .sorted(Comparator.comparing(PairwiseSimilarity::getPercent).reversed())
            .collect(Collectors.toList());

        return PageUtils.toPage(sorted, pageable);
    }

    private Triple<Double, Double, Map<String, CharacteristicsInfo>> processPairs(ProcessedOntology ont1,
                                                                                  ProcessedOntology ont2) {
        Map<String, CharacteristicsInfo> characteristicsMap = new HashMap<>();
        double sum = 0;
        double total = 0;
        for (CharacteristicsType type : CharacteristicsType.values()) {
            List<Pair<String, ProcessedOntology>> pairs = getCharacteristicsPairs(List.of(ont1, ont2), type);
            Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
            List<Similarity> list = getSimilarityList(map, false);
            List<String> similarities = list.stream().map(Similarity::getName).collect(Collectors.toList());
            sum += similarities.size() * settings.getWeight().getOrDefault(type.name().toLowerCase(), 0d);
            total += Math.min(type.getCharacteristics(ont1).size(), type.getCharacteristics(ont2).size())
                * settings.getWeight().getOrDefault(type.name().toLowerCase(), 0d);
            characteristicsMap.put(type.name().toLowerCase(), CharacteristicsInfo.of(similarities));
        }

        return Triple.of(sum, total, characteristicsMap);
    }


    private PairwiseSimilarity buildPairwiseSimilarities(ProcessedOntology ont1,
                                                         ProcessedOntology ont2,
                                                         Map<String, CharacteristicsInfo> characteristicsMap,
                                                         double sum,
                                                         double totalSum) {
        return PairwiseSimilarity.builder()
            .pair(Pair.of(ont1.getOntologyId(), ont2.getOntologyId()))
            .sum(sum)
            .totalSum(totalSum)
            .percent(totalSum == 0 ? 0 : 100 * sum / totalSum)
            .characteristics(characteristicsMap)
            .build();
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
            .sorted(Comparator.comparing(ProcessedOntology::getOntologyId))
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
