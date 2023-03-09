package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.OntologyDto;

import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.service.ProcessedOntologyService;
import eu.tib.ts.utils.HttpUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/ontology")
public class MappingController {
    private final ProcessedOntologyService processedOntologyService;
    private final OntologyFilterService ontologyFilterService;

    @Autowired
    public MappingController(
            OntologyFilterService ontologyFilterService,
            ProcessedOntologyService processedOntologyService
    ) {

        this.ontologyFilterService=ontologyFilterService;
        this.processedOntologyService = processedOntologyService;

    }

    @ApiOperation("List of all mappings between ontologies within selected collection")
    @GetMapping(value = "/mapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OntologyDto>> getMappingList(
            @ApiParam(value = "Collection to filter set of ontologies", example = "CoyPu")
            @RequestParam(required = true) List<String> collection,
            Pageable pageable
    ) {

        System.out.println("List of all mappings between ontologies within selected collection");

        List<OntologyDto> ontologyList =  processedOntologyService.getMappingOntologies();

        List<OntologyDto> ontologyDtoListFilterByCollection = new ArrayList<OntologyDto>();

        /**
         * Iterates through the collections and extracts only those onologies which belong to
         * selected collection.
         */

        for(OntologyDto dto: ontologyList){

            for(String s : collection) {

                if (dto.getCollection().contains(s)) {

                System.out.println("dto.getCollection(): " + dto.getCollection() + " dto.getUri(): " + dto.getUri());

                ontologyDtoListFilterByCollection.add(dto);

                }
            }
        }

    return HttpUtils.ok(ontologyDtoListFilterByCollection);

    }
}