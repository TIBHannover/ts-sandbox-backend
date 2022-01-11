package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.RatioDto;
import eu.tib.ts.model.ontology.SimpleOntology;
import eu.tib.ts.service.ratio.RatioService;
import eu.tib.ts.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ontology/ratio")
public class RatioController {
    private final RatioService ratioByPropertyService;

    public RatioController(
        @Qualifier("ratioByPropertyServiceImpl") RatioService ratioByPropertyService
    ) {
        this.ratioByPropertyService = ratioByPropertyService;
    }

    @GetMapping(value = "/property", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RatioDto> getRationByProperty(
        @RequestBody List<SimpleOntology> ontologies
    ) {
        double ratio = ratioByPropertyService.getRatio(ontologies);

        return HttpUtils.ok(new RatioDto(ratio));
    }
}
