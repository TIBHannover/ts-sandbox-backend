package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.ProcessedTiva;
import eu.tib.tiva.service.PreProcessingTivaService;
import eu.tib.tiva.service.ProcessedTivaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class PreProcessingTivaServiceImpl implements PreProcessingTivaService {

    private final ProcessedTivaService processedTivaService;


    @Autowired
    PreProcessingTivaServiceImpl( ProcessedTivaService processedTivaService){

        this.processedTivaService=processedTivaService;
    }

    @Override
    public ProcessedTiva preProcess(Optional TsTiva, String queryFilePath, String title) {


        return null;


    }


}
