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
import java.util.Optional;

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

    @PostMapping(value = "/{characteristics}/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForInternalOntology(
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @RequestBody List<SimpleOntology> ontologies,
        @RequestParam Optional<String> collection,
        Pageable pageable
    ) {
        Page<Similarity> page =
            similarityService.getSimilarities(ontologies, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @PostMapping(value = "/{characteristics}/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForExternalOntology(
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @RequestBody ExternalOntology ontology,
        @RequestParam Optional<String> collection,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(ontology, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }
}
