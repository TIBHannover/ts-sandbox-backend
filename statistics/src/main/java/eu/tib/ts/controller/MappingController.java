package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;
import eu.tib.ts.controller.dto.OntologyDto;

import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.service.ProcessedMappingService;
import eu.tib.ts.service.ProcessedOntologyService;
import eu.tib.ts.utils.HttpUtils;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/ontology")
public class MappingController {
    private final ProcessedOntologyService processedOntologyService;
    private final OntologyFilterService ontologyFilterService;

    private final ProcessedMappingService processedMappingService;

    @Autowired
    public MappingController(
            OntologyFilterService ontologyFilterService,
            ProcessedMappingService processedMappingService,
            ProcessedOntologyService processedOntologyService
    ) {

        this.ontologyFilterService=ontologyFilterService;
        this.processedOntologyService = processedOntologyService;
        this.processedMappingService = processedMappingService;

    }

    @Operation(summary="List mappings between a pair of ontologies grouped by source ontology")
    @GetMapping(value = "/groupedmappings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MappingGropedBySourceOntologyDto>> getMappingsGroupedBySourceOntology() {

        List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoList =
                processedMappingService.getAllMappingsGroupedBySourceOntology();

        List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoListFillteredByMappingId = new ArrayList<>();

        for(MappingGropedBySourceOntologyDto mappingGroupedBySourceOntologyDto: mappingGropedBySourceOntologyDtoList){

        mappingGropedBySourceOntologyDtoListFillteredByMappingId.add(mappingGroupedBySourceOntologyDto);

        }

    return HttpUtils.ok(mappingGropedBySourceOntologyDtoListFillteredByMappingId);

    }

    @Operation(summary="Filter mappings by selected one or more collection")
    @GetMapping(value="/gourpedmappings/filterby", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MappingGropedBySourceOntologyDto>> getMappingsFilteredByCollectionNames(
            @Parameter(description = "Filter set of mappings for source ontologies that belong " +
                    "to given collections", example = "NFDI4ING")
            @RequestParam List<String> collection,
            Pageable pageable
    ){

        List<MappingGropedBySourceOntologyDto> mappingFilteredByCollectionNameoList =
                processedMappingService.getAllMappingsGroupedBySourceOntology();

        return HttpUtils.ok(mappingFilteredByCollectionNameoList);
    }

}