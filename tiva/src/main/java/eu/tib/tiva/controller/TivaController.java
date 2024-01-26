package eu.tib.tiva.controller;


import eu.tib.tiva.controller.dto.TivaDto;
import eu.tib.tiva.service.ProcessedTivaService;
import eu.tib.tiva.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/")
public class TivaController {

    private final ProcessedTivaService processedTivaService;

    @Autowired
    public TivaController(ProcessedTivaService processedTivaService){

        this.processedTivaService=processedTivaService;
    }

    @Operation(summary = "List of all country codes")
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TivaDto>> getCountryCodeList() {

        log.info("List of all country codes");
        return HttpUtils.ok(processedTivaService.getCountryCodes());

    }


}