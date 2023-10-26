package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.repository.ProcessedMongoMappingRepository;
import eu.tib.ts.service.MappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MappingServiceImpl implements MappingService {

    private final ProcessedMongoMappingRepository processedMongoOntologyRepository;

    public MappingServiceImpl(ProcessedMongoMappingRepository processedMongoOntologyRepository) {
        this.processedMongoOntologyRepository = processedMongoOntologyRepository;
    }

    public List<ProcessedMapping> getProcessedMappings(List<String> collections) {


        return processedMongoOntologyRepository.findMappingByCollection(collections);

    }

}
