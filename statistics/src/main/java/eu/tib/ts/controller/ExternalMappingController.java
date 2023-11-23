package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.ExternalMappingModel;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.service.PreProcessingOntologyService;
import eu.tib.ts.utils.HttpUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/ontology/external")
public class ExternalMappingController {

    private final PreProcessingOntologyService preProcessingOntologyService;

    public ExternalMappingController(
        PreProcessingOntologyService preProcessingOntologyService
    ){
        this.preProcessingOntologyService=preProcessingOntologyService;
    }
    @Operation(summary = "Mappings between an external ontology and a set of selected TIB TS ontologies")
    @GetMapping(value = "/mapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ExternalMappingModel>> getMappingsForExternalOntologyUri(
            @Parameter(description = "External resolvable ontology URI")
            @RequestParam String uri,
            @Parameter(description = "Set of selected ontologies from TIB TS", example = "dr,coy,cidoc")
            @RequestParam List<String> tsOntologyList,
            Pageable pageable
    ) {

        ProcessedOntology eternalOntology = preProcessingOntologyService.preProcess(Optional.empty(), uri,"get mappings between external ontology and " +
                "selected ontologies from TIB Terminology Service");

        PagedModel<ExternalMappingModel> pagedModel = null;

        return HttpUtils.ok(pagedModel);
    }

}