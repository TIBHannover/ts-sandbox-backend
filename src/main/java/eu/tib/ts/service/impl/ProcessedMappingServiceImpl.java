package eu.tib.ts.service.impl;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.repository.ProcessedMongoMappingRepository;
import eu.tib.ts.service.ProcessedMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<MappingDto> getAllMappings() {
        return Lists.newArrayList(repository.findAll()).stream()
                .map(MappingDto::getMappingsDto)
                .sorted(Comparator.comparing(MappingDto::getMappingId))
                .collect(Collectors.toList());
    }

    @Override
    public ProcessedMapping save(ProcessedMapping processedMapping) {
        return repository.save(processedMapping);
    }
}