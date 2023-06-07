package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.PairwiseSimilarityModelAssembler;
import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.controller.dto.PairwiseSimilarityModel;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.model.ontology.SimilarityModel;
import eu.tib.ts.service.PreProcessingOntologyService;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.HttpUtils;
import eu.tib.ts.utils.PageUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    private final PreProcessingOntologyService preProcessingOntologyService;
    private final SimilarityModelAssembler modelAssembler;
    private final PairwiseSimilarityModelAssembler pairwiseSimilarityModelAssembler;
    private final PagedResourcesAssembler<Similarity> pagedResourcesAssembler;
    private final PagedResourcesAssembler<PairwiseSimilarity> pairwiseSimilarityPagedResourcesAssembler;

    @Autowired
    public SimilarityController(
        SimilarityService similarityService,
        PreProcessingOntologyService preProcessingOntologyService,
        SimilarityModelAssembler modelAssembler,
        PairwiseSimilarityModelAssembler pairwiseSimilarityModelAssembler,
        PagedResourcesAssembler<Similarity> pagedResourcesAssembler,
        PagedResourcesAssembler<PairwiseSimilarity> pairwiseSimilarityPagedResourcesAssembler
    ) {
        this.similarityService = similarityService;
        this.preProcessingOntologyService = preProcessingOntologyService;
        this.modelAssembler = modelAssembler;
        this.pairwiseSimilarityModelAssembler = pairwiseSimilarityModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.pairwiseSimilarityPagedResourcesAssembler = pairwiseSimilarityPagedResourcesAssembler;
    }

    @ApiOperation(value = "Similarity measure between TS internal ontologies " +
        "by calculating shared Properties | Classes | Imports | Namespaces")
    @GetMapping(value = "/{characteristics}/internal/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForInternalOntologyList(
        @ApiParam(value = "Characteristics to be compared by", example = "PROPERTY")
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @ApiParam(value = "A set of Ontology IDs managed in the TS", example = "dicl,dicob")
        @RequestParam List<String> ids,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(ids, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel =
            PageUtils.toPagedModel(page, SimilarityModel.class, pagedResourcesAssembler, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation(value = "Similarity measure between given TS internal ontology" +
        "and set of TS internal ontologies")
    @GetMapping(value = "/{characteristics}/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForInternalOntology(
        @ApiParam(value = "Characteristics to be compared by", example = "PROPERTY")
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @ApiParam(value = "A given Ontology ID managed in the TS", example = "swo")
        @RequestParam String id,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSimilarities(id, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel =
            PageUtils.toPagedModel(page, SimilarityModel.class, pagedResourcesAssembler, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation(value = "Similarity measure for external ontology " +
        "by calculating shared Properties | Classes | Imports | Namespaces")
    @GetMapping(value = "/{characteristics}/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityForExternalOntology(
        @ApiParam(value = "Characteristics to be compared by", example = "PROPERTY")
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @ApiParam(value = "External ontology URL")
        @RequestParam String url,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        ProcessedOntology ontology = preProcessingOntologyService.preProcess(Optional.empty(), url,"getSimilarityForExternalOntology");
        Page<Similarity> page = similarityService.getSimilarities(ontology, characteristicsType, collection, pageable);
        PagedModel<SimilarityModel> pagedModel =
            PageUtils.toPagedModel(page, SimilarityModel.class, pagedResourcesAssembler, modelAssembler);


        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation("Pairwise similarity between TS internal ontologies")
    @GetMapping(value = "/pairwise/internal/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getPairwiseSimilarityForInternalOntologyList(
        @ApiParam(value = "A set of Ontology IDs managed in the TS", example = "dicl,dicob")
        @RequestParam(required = false) Optional<List<String>> ids,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<PairwiseSimilarity> page = similarityService.getPairwiseSimilarity(ids, collection, pageable);

        PagedModel<PairwiseSimilarityModel> pagedModel = PageUtils.toPagedModel(
            page,
            PairwiseSimilarityModel.class,
            pairwiseSimilarityPagedResourcesAssembler,
            pairwiseSimilarityModelAssembler
        );

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation("Pairwise similarity between given TS internal ontology " +
        "and a set of TS internal ontologies")
    @GetMapping(value = "/pairwise/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getPairwiseSimilarityForInternalOntology(
        @ApiParam(value = "A given Ontology ID managed in the TS", example = "dicl")
        @RequestParam String id,
        @ApiParam(value = "A set of Ontology IDs managed in the TS", example = "dicl,dicob")
        @RequestParam(required = false) Optional<List<String>> ids,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<PairwiseSimilarity> page = similarityService.getPairwiseSimilarity(id, ids, collection, pageable);

        PagedModel<PairwiseSimilarityModel> pagedModel = PageUtils.toPagedModel(
            page,
            PairwiseSimilarityModel.class,
            pairwiseSimilarityPagedResourcesAssembler,
            pairwiseSimilarityModelAssembler
        );

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation("Pairwise similarity for external ontology")
    @GetMapping(value = "/pairwise/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getPairwiseSimilarityForExternalOntology(
        @ApiParam(value = "External ontology URL")
        @RequestParam String url,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        ProcessedOntology ontology = preProcessingOntologyService.preProcess(Optional.empty(), url,"getPairwiseSimilarityForExternalOntology");
        Page<PairwiseSimilarity> page = similarityService.getPairwiseSimilarity(ontology, collection, pageable);
        PagedModel<PairwiseSimilarityModel> pagedModel = PageUtils.toPagedModel(
            page,
            PairwiseSimilarityModel.class,
            pairwiseSimilarityPagedResourcesAssembler,
            pairwiseSimilarityModelAssembler
        );

        return HttpUtils.ok(pagedModel);
    }

    @ApiOperation("Pairwise similarity for external ontology")
    @GetMapping(value = "/pairwise/external/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseSimilarityModel>> getPairwiseSimilarityForExternalOntologyList(
        @ApiParam(value = "External ontology URL")
        @RequestParam String url,
        @ApiParam(value = "A set of Ontology IDs managed in the TS", example = "dicl,dicob")
        @RequestParam(required = false) Optional<List<String>> ids,
        @ApiParam(value = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        ProcessedOntology ontology = preProcessingOntologyService.preProcess(Optional.empty(), url,"getPairwiseSimilarityForExternalOntologyList");
        Page<PairwiseSimilarity> page = similarityService.getPairwiseSimilarity(ontology, ids, collection, pageable);
        PagedModel<PairwiseSimilarityModel> pagedModel = PageUtils.toPagedModel(
            page,
            PairwiseSimilarityModel.class,
            pairwiseSimilarityPagedResourcesAssembler,
            pairwiseSimilarityModelAssembler
        );

        return HttpUtils.ok(pagedModel);
    }
}
