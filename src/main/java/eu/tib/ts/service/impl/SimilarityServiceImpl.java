package eu.tib.ts.service.impl;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.SimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SimilarityServiceImpl implements SimilarityService {
    private final ProcessedOntologyRepository processedOntologyRepository;

    @Autowired
    public SimilarityServiceImpl(ProcessedOntologyRepository processedOntologyRepository) {
        this.processedOntologyRepository = processedOntologyRepository;
    }

    @Override
    public <T extends Ontology> Page<Similarity> getSharedPropertyUri(List<T> ontologies, Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies(ontologies);

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String item : processedOntology.getProperties()) {
                pairs.add(Pair.of(item, processedOntology));
            }
        }

        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);

        List<Similarity> list = map.entrySet().stream()
            .map(entry -> Similarity.builder()
                .name(entry.getKey())
                .ontologies(entry.getValue())
                .build()
            )
            .collect(Collectors.toList());

        List<Similarity> slice = list.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());

        return new PageImpl<>(slice, pageable, list.size());
    }

    @Override
    public <T extends ExtendedOntology> Page<Similarity> getSharedPropertyUri(T ontology, Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String item : processedOntology.getProperties()) {
                if (!ontology.getProperties().contains(item)) {
                    continue;
                }
                pairs.add(Pair.of(item, processedOntology));
            }
        }

        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);

        List<Similarity> list = map.entrySet().stream()
            .map(entry -> Similarity.builder()
                .name(entry.getKey())
                .ontologies(entry.getValue())
                .build()
            )
            .collect(Collectors.toList());

        List<Similarity> slice = list.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());

        return new PageImpl<>(slice, pageable, list.size());
    }

    @Override
    public <T extends Ontology> Page<Similarity> getSharedClassUri(List<T> ontologies, Pageable pageable) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies(ontologies);

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String item : processedOntology.getClasses()) {
                pairs.add(Pair.of(item, processedOntology));
            }
        }

        Map<String, List<OntologyDto>> map = getSimilarityMap(pairs);

        List<Similarity> list = map.entrySet().stream()
            .map(entry -> Similarity.builder()
                .name(entry.getKey())
                .ontologies(entry.getValue())
                .build()
            )
            .collect(Collectors.toList());

        List<Similarity> slice = list.stream()
            .skip((long) pageable.getPageNumber() * pageable.getPageSize())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());

        return new PageImpl<>(slice, pageable, list.size());
    }

    private <T extends Ontology> List<ProcessedOntology> getProcessedOntologies(List<T> ontologies) {
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        return processedOntologyRepository.findByOntologyIdIn(ids);
    }

    private <T extends Ontology> List<ProcessedOntology> getProcessedOntologies() {

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
