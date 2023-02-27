package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.MappingModelAssembler;
import eu.tib.ts.controller.assember.PairwiseMappingModelAssembler;

import eu.tib.ts.model.ontology.*;

import eu.tib.ts.model.ontology.Mapping;

import eu.tib.ts.service.MappingService;
import eu.tib.ts.service.PreProcessingOntologyService;

import eu.tib.ts.utils.HttpUtils;
import eu.tib.ts.utils.PageUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
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
    private final MappingService mappingService;
    private final PreProcessingOntologyService preProcessingOntologyService;

    private final MappingModelAssembler mappingModelAssembler;

    private final PairwiseMappingModelAssembler pairwiseMappingModelAssembler;
    private final PagedResourcesAssembler<Mapping> pagedResourcesAssembler;

    private final PagedResourcesAssembler<PairwiseMapping> pairwiseMappingPagedResourcesAssembler;

    @Autowired
    public MappingController(
            MappingService mappingService,
            PreProcessingOntologyService preProcessingOntologyService,
            MappingModelAssembler mappingModelAssembler,
            PairwiseMappingModelAssembler pairwiseMappingModelAssembler,
            PagedResourcesAssembler<Mapping> pagedResourcesAssembler,
            PagedResourcesAssembler<PairwiseMapping> pairwiseMappingPagedResourcesAssembler
    ) {
        this.mappingService = mappingService;
        this.preProcessingOntologyService = preProcessingOntologyService;
        this.mappingModelAssembler = mappingModelAssembler;
        this.pairwiseMappingModelAssembler = pairwiseMappingModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.pairwiseMappingPagedResourcesAssembler = pairwiseMappingPagedResourcesAssembler;
    }

    @ApiOperation(value = "Mapping between internal ontologies in TS")
    @GetMapping(value = "/{characteristics}/mapping/internal/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<MappingModel>> getMappingForInternalOntologyList(
            @ApiParam(value = "Characteristics to be mapped by", example = "CLASS")
            @PathVariable("characteristics") CharacteristicsType characteristicsType,
            @ApiParam(value = "A set of Ontology IDs managed in the TS", example = "dr")
            @RequestParam List<String> ids,
            @ApiParam(value = "Collection to filter set of ontologies", example = "CoyPu")
            @RequestParam(required = false) Optional<String> collection,
            Pageable pageable
    ) {
        Page<Mapping> page = mappingService.getMappings(ids, characteristicsType, collection, pageable);

        PagedModel<MappingModel> pagedModel =
                PageUtils.toPagedModel(page, MappingModel.class, pagedResourcesAssembler, mappingModelAssembler);

        return HttpUtils.ok(pagedModel);
    }
}