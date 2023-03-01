package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.PairwiseMappingModelAssembler;
import eu.tib.ts.controller.dto.MappingDto;

import eu.tib.ts.controller.dto.PairwiseMappingModel;
import eu.tib.ts.controller.dto.PairwiseSimilarityModel;
import eu.tib.ts.model.ontology.PairwiseMapping;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.service.MappingService;
import eu.tib.ts.service.ProcessedOntologyService;
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
@RequestMapping("api/ontology")
public class MappingController {
    private final ProcessedOntologyService processedOntologyService;
    private final MappingService mappingService;
    private final PairwiseMappingModelAssembler pairwiseMappingModelAssembler;
    private final PagedResourcesAssembler<PairwiseMapping> pairwiseMappingPagedResourcesAssembler;

    @Autowired
    public MappingController(
            PagedResourcesAssembler<PairwiseMapping> pairwiseMappingPagedResourcesAssembler,
            PairwiseMappingModelAssembler pairwiseMappingModelAssembler,
            MappingService mappingService,
            ProcessedOntologyService processedOntologyService
    ) {
        this.pairwiseMappingModelAssembler = pairwiseMappingModelAssembler;
        this.mappingService = mappingService;
        this.processedOntologyService = processedOntologyService;
        this.pairwiseMappingPagedResourcesAssembler = pairwiseMappingPagedResourcesAssembler;
    }

    @ApiOperation("List of all mappings between ontologies within selected collection")
    @GetMapping(value = "/mapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<PairwiseMappingModel>> getMappingList(
            @ApiParam(value = "Collection to filter set of ontologies", example = "CoyPu")
            @RequestParam(required = true) Optional<String> collection,
            Pageable pageable
    ) {

        System.out.println("List of all mappings between ontologies in selected collection");
        Page<PairwiseMapping> page = mappingService.getPiarwiseMapping(collection, pageable);

        PagedModel<PairwiseMappingModel> pagedModel = PageUtils.toPagedModel(
                page,
                PairwiseMappingModel.class,
                pairwiseMappingPagedResourcesAssembler,
                pairwiseMappingModelAssembler
        );
        return HttpUtils.ok(pagedModel);
    }
}