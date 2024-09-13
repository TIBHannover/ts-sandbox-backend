package eu.tib.ts.service.impl;

import eu.tib.ts.service.MappingFilterService;
import eu.tib.ts.service.MappingGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MappingGraphServiceImpl implements MappingGraphService {

    private final eu.tib.ts.repository.ProcessedMongoOntologyRepository ProcessedMongoOntologyRepository;

    private final MappingFilterService mappingFilterService;

    @Autowired
    public MappingGraphServiceImpl(eu.tib.ts.repository.ProcessedMongoOntologyRepository ProcessedMongoOntologyRepository,
                                       MappingFilterService mappingFilterService) {
        this.ProcessedMongoOntologyRepository = ProcessedMongoOntologyRepository;
        this.mappingFilterService = mappingFilterService;
    }
}