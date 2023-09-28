package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.service.MappingFilterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MappingFilterServiceImpl implements MappingFilterService {

    @Override
    public List<ProcessedMapping> filterMappings(List<ProcessedMapping> processedMappings, Optional<String> collection) {



        return null;
    }
}
