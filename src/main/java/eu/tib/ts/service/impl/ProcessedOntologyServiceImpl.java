package eu.tib.ts.service.impl;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedOntologyRepository;
import eu.tib.ts.service.ProcessedOntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessedOntologyServiceImpl implements ProcessedOntologyService {
    private final ProcessedOntologyRepository repository;

    @Autowired
    public ProcessedOntologyServiceImpl(ProcessedOntologyRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProcessedOntology> findAll() {
        return Lists.newArrayList(repository.findAll());
    }

    @Override
    public List<OntologyDto> getOntologies() {
        return Lists.newArrayList(repository.findAll()).stream()
            .map(OntologyDto::of)
            .sorted(Comparator.comparing(OntologyDto::getOntologyId))
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getOntologyIds() {
        return Lists.newArrayList(repository.findAll()).stream()
            .map(ProcessedOntology::getOntologyId)
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public ProcessedOntology save(ProcessedOntology processedOntology) {
        return repository.save(processedOntology);
    }
}
