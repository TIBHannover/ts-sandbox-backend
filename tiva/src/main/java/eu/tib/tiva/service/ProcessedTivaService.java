package eu.tib.tiva.service;

import eu.tib.tiva.model.ProcessedTiva;

import java.util.List;

public interface ProcessedTivaService {

    public List<ProcessedTiva> findAll();

public List<String> getAllCountryCodes();



    ProcessedTiva save (ProcessedTiva processedTiva);


}
