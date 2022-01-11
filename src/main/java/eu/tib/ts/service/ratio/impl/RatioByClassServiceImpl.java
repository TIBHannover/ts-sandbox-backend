package eu.tib.ts.service.ratio.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.ratio.RatioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("ratioByClassServiceImpl")
public class RatioByClassServiceImpl extends RatioAbstractService implements RatioService {

    @Autowired
    public RatioByClassServiceImpl(ProcessedOntologyRepository processedOntologyRepository) {
        super(processedOntologyRepository);
    }

    @Override
    protected List<Set<String>> getCharacteristics(
        List<ProcessedOntology> processedOntologies
    ) {
        return processedOntologies.stream()
            .map(ProcessedOntology::getClasses)
            .collect(Collectors.toList());
    }
}
