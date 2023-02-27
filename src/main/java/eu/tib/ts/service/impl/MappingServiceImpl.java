package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.MappingDto;

import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.Mapping;
import eu.tib.ts.model.ontology.ProcessedOntology;

import eu.tib.ts.repository.ProcessedMongoOntologyRepository;
import eu.tib.ts.service.MappingService;
import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MappingServiceImpl implements MappingService {

    private final ProcessedMongoOntologyRepository ProcessededMongoOntologyRepository;
    private final OntologyFilterService ontologyFilterService;
    private final MappingSetttings mappingSetttings;

    public MappingServiceImpl(
            ProcessedMongoOntologyRepository ProcessededMongoOntologyRepository,
            OntologyFilterService ontologyFilterService,
            MappingSetttings mappingSetttings){

        this.ProcessededMongoOntologyRepository = ProcessededMongoOntologyRepository;
        this.ontologyFilterService = ontologyFilterService;
        this.mappingSetttings = mappingSetttings;
    }

    @Override
    public Page<Mapping> getMappings(List<String> ids, CharacteristicsType characteristicsType, Optional<String> collection, Pageable pageable) {

        List<ProcessedOntology> processedOntologyList = getProcessedOntologiesMapping(ids);

        if(processedOntologyList == null || processedOntologyList.isEmpty()){
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        List<ProcessedOntology>  filteredOntologies = ontologyFilterService.filter(processedOntologyList, collection);
        List<Pair<String, ProcessedOntology>> pairs = getCharacteristicsPairs(filteredOntologies, characteristicsType);

        Map<String, List<MappingDto>> map = getMappingMap(pairs);
        List<Mapping> list = geMappingList(map, false);

        return PageUtils.toPage(list, pageable);

    }

    private List<ProcessedOntology> getProcessedOntologiesMapping(List<String> ids){

    return ProcessededMongoOntologyRepository.findByOntologyIdIn(ids);

    }

    /**
     * Copied and datapted from SimilarityServiceImpl class
     *
     * @param processedOntologies
     * @param characteristicsType
     * @return
     */
    private List<Pair<String, ProcessedOntology>> getCharacteristicsPairs(
            List<ProcessedOntology> processedOntologies,
            CharacteristicsType characteristicsType
    ) {
        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String characteristics : characteristicsType.getCharacteristics(processedOntology)) {
                pairs.add(Pair.of(characteristics, processedOntology));
            }
        }

        return pairs;
    }

    /**
     *
     * Copied and adapted from SimilarityServiceImpl class
     *
     * @param pairs
     * @return
     */
    private Map<String, List<MappingDto>> getMappingMap(List<Pair<String, ProcessedOntology>> pairs) {
        Map<String, List<MappingDto>> mappingMap = pairs.stream()
                .collect(
                        Collectors.groupingBy(
                                Pair::getFirst,
                                Collectors.mapping(pair -> MappingDto.of(pair.getSecond()),
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                list -> list.stream()
                                                        .sorted(Comparator.comparing(MappingDto::getMappingId))
                                                        .collect(Collectors.toList())
                                        )
                                )
                        )
                );

        return mappingMap.entrySet().stream()
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

    /**
     *
     * Copied and adapted from SimilartyServiceImpl
     *
     * @param map
     * @param external
     * @return
     */
    private List<Mapping> geMappingList(Map<String, List<MappingDto>> map, boolean external) {
        return map.entrySet().stream()
                .filter(entry -> external || entry.getValue().size() > 1)
                .map(entry -> Mapping.builder()
                        .name(entry.getKey())
                        .mappingDtoList(entry.getValue())
                        .build()
                )
                .collect(Collectors.toList());
    }


}
