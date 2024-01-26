package eu.tib.tiva.service;

import eu.tib.tiva.model.ProcessedTiva;

import java.util.Optional;

public interface PreProcessingTivaService {

    ProcessedTiva preProcess(Optional TsTiva, String queryFilePath, String title);
}
