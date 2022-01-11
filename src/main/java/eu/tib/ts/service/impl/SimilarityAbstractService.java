package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.utils.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class SimilarityAbstractService {
    private final ProcessedOntologyRepository processedOntologyRepository;

    protected SimilarityAbstractService(ProcessedOntologyRepository processedOntologyRepository) {
        this.processedOntologyRepository = processedOntologyRepository;
    }

    protected abstract List<Pair<String, ProcessedOntology>> getCharacteristicsPairs(
        List<ProcessedOntology> processedOntologies
    );

    protected abstract <T extends ExtendedOntology> List<Pair<String, ProcessedOntology>> getCharacteristicsPairs(
        List<ProcessedOntology> processedOntologies, T ontology
    );

    public <T extends Ontology> Page<Similarity> getSimilarities(List<T> ontologies, Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies(ontologies);
        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        List<Pair<String, ProcessedOntology>> pairs = getCharacteristicsPairs(processedOntologies);
        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
        List<Similarity> list = getSimilarityList(map, false);

        return PageUtils.toPage(list, pageable);
    }

    public <T extends ExtendedOntology> Page<Similarity> getSimilarities(T ontology, Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();
        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        ProcessedOntology externalOntology = ProcessedOntology.of(ontology);
        processedOntologies.add(externalOntology);

        List<Pair<String, ProcessedOntology>> pairs = getCharacteristicsPairs(processedOntologies, ontology);
        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);
        List<Similarity> list = getSimilarityList(map, true);

        return PageUtils.toPage(list, pageable);
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

    private <T extends Ontology> List<ProcessedOntology> getProcessedOntologies(List<T> ontologies) {
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

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
