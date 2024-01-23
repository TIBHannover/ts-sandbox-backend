package eu.tib.tiva.service;

import eu.tib.tiva.model.ProcessedTiva;

import java.util.List;

public interface ProcessedTivaService {

    List<ProcessedTiva> findAll();

List<String> getAllCountryCodes();

}
