package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.repository.ProcessedMongoMappingRepository;
import eu.tib.ts.service.ProcessedMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessedMappingServiceImpl implements ProcessedMappingService {
    private final ProcessedMongoMappingRepository repository;

    @Autowired
    public ProcessedMappingServiceImpl(ProcessedMongoMappingRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public SequenceGeneratorService sequenceGeneratorService;



    @Override
    public ProcessedMapping save(ProcessedMapping processedMapping) {
        return repository.save(processedMapping);
    }
}