package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.model.ontology.*;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ontology/similarity")
public class SimilarityController {
    private final SimilarityService similarityService;
    private final SimilarityModelAssembler modelAssembler;
    private final PagedResourcesAssembler<Similarity> pagedResourcesAssembler;

    @Autowired
    public SimilarityController(
        SimilarityService similarityService,
        SimilarityModelAssembler modelAssembler,
        PagedResourcesAssembler<Similarity> pagedResourcesAssembler
    ) {
        this.similarityService = similarityService;
        this.modelAssembler = modelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping(value = "/{characteristics}/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForInternalOntology(
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @RequestBody List<SimpleOntology> ontologies,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(ontologies, characteristicsType, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @GetMapping(value = "/{characteristics}/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForExternalOntology(
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @RequestBody ExternalOntology ontology,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(ontology, characteristicsType, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }
}
