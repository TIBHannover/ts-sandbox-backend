package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.ProcessedTiva;
import eu.tib.tiva.service.PreProcessingTivaService;
import eu.tib.tiva.service.ProcessedTivaService;

import java.util.Optional;

public class PreProcessingTivaServiceImpl implements PreProcessingTivaService {

    private final ProcessedTivaService processedTivaService;


    PreProcessingTivaServiceImpl( ProcessedTivaService processedTivaService){

        this.processedTivaService=processedTivaService;
    }

    @Override
    public ProcessedTiva preProcess(Optional TsTiva, String queryFilePath, String title) {


        return null;


    }


}
