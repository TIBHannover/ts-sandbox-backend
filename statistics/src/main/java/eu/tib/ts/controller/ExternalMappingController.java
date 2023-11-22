package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.PairwiseSimilarityModel;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.utils.HttpUtils;
import eu.tib.ts.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/ontology/external")
public class ExternalMappingController {

    @Operation(summary = "Mappings between an external ontology and a set of selected TIB TS ontologies")
    @GetMapping(value = "/mapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getMappingsForExternalOntologyUri(
            @Parameter(description = "External resolvable ontology URI")
            @RequestParam String uri,
            @Parameter(description = "Set of selected ontologies from TIB TS", example = "dr,coy,cidoc")
            @RequestParam List<String> tsOntologyList,
            Pageable pageable
    ) {

        PagedModel<PairwiseSimilarityModel> pagedModel = null;

        return HttpUtils.ok(pagedModel);
    }

}