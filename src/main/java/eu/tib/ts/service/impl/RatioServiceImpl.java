package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.RatioService;
import eu.tib.ts.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RatioServiceImpl implements RatioService {
    private final ProcessedOntologyRepository processedOntologyRepository;

    @Autowired
    protected RatioServiceImpl(ProcessedOntologyRepository processedOntologyRepository) {
        this.processedOntologyRepository = processedOntologyRepository;
    }

    public <T extends Ontology> double getRatio(List<T> ontologies,
                                                CharacteristicsType characteristicsType) {
        List<ProcessedOntology> processedOntologies = getProcessedOntologies(ontologies);
        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return 0;
        }

        List<Set<String>> characteristics = processedOntologies.stream()
            .map(characteristicsType::getCharacteristics)
            .collect(Collectors.toList());

        double distinctCharacteristicsNumber = characteristics.stream()
            .flatMap(Collection::stream)
            .distinct()
            .count();

        Set<String> intersection = CollectionUtils.intersection(characteristics);

        return distinctCharacteristicsNumber == 0 ? 0 : intersection.size() / distinctCharacteristicsNumber;
    }

    private <T extends Ontology> List<ProcessedOntology> getProcessedOntologies(List<T> ontologies) {
        List<String> ids = ontologies.stream()
            .map(Ontology::getOntologyId)
            .collect(Collectors.toList());

        return processedOntologyRepository.findByOntologyIdIn(ids);
    }
}
