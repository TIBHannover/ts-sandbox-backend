package eu.tib.tiva.controller;

import eu.tib.tiva.service.ProcessedTivaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/tiva")
public class TivaController {

    private final ProcessedTivaService processedTivaService;


    public TivaController(ProcessedTivaService processedTivaService) {

        this.processedTivaService = processedTivaService;
    }
}
