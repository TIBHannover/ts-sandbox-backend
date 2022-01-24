package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.PairwiseSimilarityModelAssembler;
import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.controller.dto.PairwiseSimilarityModel;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ExternalOntology;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.model.ontology.SimilarityModel;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.HttpUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ontology/similarity")
public class SimilarityController {
    private final SimilarityService similarityService;
    private final SimilarityModelAssembler modelAssembler;
    private final PairwiseSimilarityModelAssembler pairwiseSimilarityModelAssembler;
    private final PagedResourcesAssembler<Similarity> pagedResourcesAssembler;
    private final PagedResourcesAssembler<PairwiseSimilarity> pairwiseSimilarityPagedResourcesAssembler;

    @Autowired
    public SimilarityController(
        SimilarityService similarityService,
        SimilarityModelAssembler modelAssembler,
        PairwiseSimilarityModelAssembler pairwiseSimilarityModelAssembler,
        PagedResourcesAssembler<Similarity> pagedResourcesAssembler,
        PagedResourcesAssembler<PairwiseSimilarity> pairwiseSimilarityPagedResourcesAssembler
    ) {
        this.similarityService = similarityService;
        this.modelAssembler = modelAssembler;
        this.pairwiseSimilarityModelAssembler = pairwiseSimilarityModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.pairwiseSimilarityPagedResourcesAssembler = pairwiseSimilarityPagedResourcesAssembler;
    }

    @GetMapping(value = "/{characteristics}/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForInternalOntology(
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @RequestParam List<String> ids,
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(ids, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @PostMapping(value = "/{characteristics}/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForExternalOntology(
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @RequestBody ExternalOntology ontology,
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(ontology, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation("Pairwise similarity for internal ontologies")
    @GetMapping(value = "/pairwise/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getPairwiseSimilarityForInternalOntology(
        @RequestParam(required = false) Optional<List<String>> ids,
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<PairwiseSimilarity> page = similarityService.getPairwiseSimilarity(ids, collection, pageable);
        PagedModel<PairwiseSimilarityModel> pagedModel
            = pairwiseSimilarityPagedResourcesAssembler.toModel(page, pairwiseSimilarityModelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation("Pairwise similarity for external ontologies")
    @PostMapping(value = "/pairwise/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getPairwiseSimilarityForExternalOntology(
        @RequestBody ExternalOntology ontology,
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<PairwiseSimilarity> page = similarityService.getPairwiseSimilarity(ontology, collection, pageable);
        PagedModel<PairwiseSimilarityModel> pagedModel
            = pairwiseSimilarityPagedResourcesAssembler.toModel(page, pairwiseSimilarityModelAssembler);

        return HttpUtils.ok(pagedModel);
    }
}
