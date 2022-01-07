package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.model.ontology.ExternalOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.model.ontology.SimilarityModel;
import eu.tib.ts.model.ontology.SimpleOntology;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ontology/similarity")
public class SimilarityController {
    private final SimilarityService similarityService;
    private final SimilarityModelAssembler modelAssembler;
    private final PagedResourcesAssembler<Similarity> pagedResourcesAssembler;

    @Autowired
    public SimilarityController(SimilarityService similarityService,
                                SimilarityModelAssembler modelAssembler,
                                PagedResourcesAssembler<Similarity> pagedResourcesAssembler) {
        this.similarityService = similarityService;
        this.modelAssembler = modelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping(value = "/property/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityByPropertyInternal(
        @RequestBody List<SimpleOntology> ontologies,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSharedPropertyUri(ontologies, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @GetMapping(value = "/property/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> getSimilarityByPropertyExternal(
        @RequestBody ExternalOntology ontology,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSharedPropertyUri(ontology, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }

    @GetMapping(value = "/class", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<SimilarityModel>> similarityByClass(
        @RequestBody List<SimpleOntology> ontologies,
        Pageable pageable
    ) {
        Page<Similarity> page = similarityService.getSharedClassUri(ontologies, pageable);
        PagedModel<SimilarityModel> pagedModel = pagedResourcesAssembler.toModel(page, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }
}
