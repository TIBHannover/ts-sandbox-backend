package eu.tib.tiva.service;

import eu.tib.tiva.controller.dto.TivaDto;
import eu.tib.tiva.model.ProcessedTiva;

import java.util.List;

public interface ProcessedTivaService {

    public List<ProcessedTiva> findAll();

     public List<TivaDto> getCountryCodes();

    ProcessedTiva save (ProcessedTiva processedTiva);


}
