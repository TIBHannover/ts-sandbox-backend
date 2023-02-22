package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.service.PreProcessingService;
import eu.tib.ts.service.ProcessedOntologyService;
import eu.tib.ts.utils.HttpUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ApiOperation("Ontology")
@RestController
@RequestMapping("/api/ontology")
public class OntologyController {
    private final ProcessedOntologyService ontologyService;

    @Autowired
    public OntologyController(ProcessedOntologyService ontologyService) {

        this.ontologyService = ontologyService;

    }

    @ApiOperation("List of all ontologies")
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OntologyDto>> getOntologyList() {

        System.out.println("List of all ontologies");
        return HttpUtils.ok(ontologyService.getOntologies());

    }

    @ApiOperation("List of all ontologies ids")
    @GetMapping(value = "/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getOntologyIdList() {

        System.out.println("List of all ontologies ids");

        return HttpUtils.ok(ontologyService.getOntologyIds());
    }
}
