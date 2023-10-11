package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.service.ProcessedOntologyService;
import eu.tib.ts.utils.HttpUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ontology")
public class OntologyController {
    private final ProcessedOntologyService ontologyService;

    @Autowired
    public OntologyController(ProcessedOntologyService ontologyService) {

        this.ontologyService = ontologyService;

    }

    @Operation(summary = "List of all ontologies")
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OntologyDto>> getOntologyList() {

        log.error("List of all ontologies");
        return HttpUtils.ok(ontologyService.getOntologies());

    }

    @Operation(summary = "List of all ontologies ids")
    @GetMapping(value = "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getOntologyIdList() {

        log.error("List of all ontologies ids");

        return HttpUtils.ok(ontologyService.getOntologyIds());
    }
}
