package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.PairwiseMappingModelAssembler;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.controller.dto.PairwiseMappingModel;
import eu.tib.ts.model.ontology.PairwiseMapping;
import eu.tib.ts.service.MappingService;
import eu.tib.ts.service.ProcessedOntologyService;
import eu.tib.ts.utils.HttpUtils;

import eu.tib.ts.utils.PageUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.jfree.util.Log;
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


    @Autowired
    public MappingController(
            ProcessedOntologyService processedOntologyService
    ) {
        this.processedOntologyService = processedOntologyService;

    }

    @ApiOperation("List of all mappings between ontologies within selected collection")
    @GetMapping(value = "/mapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OntologyDto>> getMappingList(
            @ApiParam(value = "Collection to filter set of ontologies", example = "CoyPu")
            @RequestParam(required = true) Optional<String> collection,
            Pageable pageable
    ) {

        System.out.println("List of all mappings between ontologies within selected collection");
        List<OntologyDto> ontologyList =  processedOntologyService.getMappingOntologies();

        for(OntologyDto dto: ontologyList){
            System.out.println("dto.getCollection(): " + dto.getCollection() +" dto.getUri(): " + dto.getUri());
        }

    return HttpUtils.ok(ontologyList);

    }
}