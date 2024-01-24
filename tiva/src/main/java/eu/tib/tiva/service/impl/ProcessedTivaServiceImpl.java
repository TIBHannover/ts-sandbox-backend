package eu.tib.tiva.service.impl;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import eu.tib.tiva.controller.dto.TivaDto;
import eu.tib.tiva.model.ProcessedTiva;
import eu.tib.tiva.repository.ProcessedMongoTivaRepository;
import eu.tib.tiva.service.ProcessedTivaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessedTivaServiceImpl implements ProcessedTivaService {

    private final ProcessedMongoTivaRepository repository;

    @Autowired
    public ProcessedTivaServiceImpl(ProcessedMongoTivaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProcessedTiva> findAll() {
        return  Lists.newArrayList(repository.findAll());
    }

    @Override
    public List<TivaDto> getCountryCodes() {
                return Lists.newArrayList(repository.findAll()).stream()
                .map(TivaDto::of)
                .sorted(Comparator.comparing(TivaDto::getTivaId))
                .collect(Collectors.toList());
    }

    @Override
    public ProcessedTiva save(ProcessedTiva processedTiva) {
        return repository.save(processedTiva);
    }

}
