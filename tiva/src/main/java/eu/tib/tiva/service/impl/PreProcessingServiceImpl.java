package eu.tib.tiva.service.impl;

import eu.tib.tiva.service.PreProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PreProcessingServiceImpl implements PreProcessingService {

    @Override
    public void doPreProcessing() {

        log.info("do tiva pre preprocessing");

    }
}