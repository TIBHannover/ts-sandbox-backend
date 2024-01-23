package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.ProcessedTiva;
import eu.tib.tiva.repository.ProcessedMongoTivaRepository;
import eu.tib.tiva.service.ProcessedTivaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProcessedTivaServiceImpl implements ProcessedTivaService {

    private final ProcessedMongoTivaRepository repository;

    @Autowired
    public ProcessedTivaServiceImpl(ProcessedMongoTivaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProcessedTiva> findAll() {
        return null;
    }

    @Override
    public List<String> getAllCountryCodes() {
        return null;

    }

    @Override
    public ProcessedTiva save(ProcessedTiva processedTiva) {
        return repository.save(processedTiva);
    }

}
