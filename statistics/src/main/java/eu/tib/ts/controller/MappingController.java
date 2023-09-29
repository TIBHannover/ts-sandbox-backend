package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;
import eu.tib.ts.controller.dto.OntologyDto;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.service.MappingFilterService;
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

    private final ProcessedMappingService processedMappingService;

    private final MappingFilterService mappingFilterService;

    @Autowired
    public MappingController(
            ProcessedMappingService processedMappingService,
            MappingFilterService mappingFilterService
    ) {

        this.processedMappingService = processedMappingService;
        this.mappingFilterService = mappingFilterService;

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
            @RequestParam Optional<String> collection,
            Pageable pageable
    ){

        List<ProcessedMapping> processedMappings =null ; // getMappingsFilteredByCollection(collection);

        List<ProcessedMapping> filteredMappingsByCollection = mappingFilterService.filterMappings(processedMappings, collection);


        List<MappingGropedBySourceOntologyDto> mappingFilteredByCollectionNameoList =
                processedMappingService.getMappingsFilteredByCollection(collection);

        return HttpUtils.ok(mappingFilteredByCollectionNameoList);
    }

}