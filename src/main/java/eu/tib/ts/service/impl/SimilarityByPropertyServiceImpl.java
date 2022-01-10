package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.SimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("similarityByPropertyServiceImpl")
public class SimilarityByPropertyServiceImpl extends SimilarityAbstractService implements SimilarityService {

    @Autowired
    public SimilarityByPropertyServiceImpl(ProcessedOntologyRepository processedOntologyRepository) {
        super(processedOntologyRepository);
    }

    @Override
    protected List<Pair<String, ProcessedOntology>> getCharacteristicsPairs(
        List<ProcessedOntology> processedOntologies
    ) {
        List<Pair<String, ProcessedOntology>> pairs = new ArrayList<>();
        for (ProcessedOntology processedOntology : processedOntologies) {
            for (String item : processedOntology.getProperties()) {
                pairs.add(Pair.of(item, processedOntology));
            }
        }

        return pairs;
    }
}
