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

//    @Operation(summary = "List of all ontologies within selected collection")
//    @GetMapping(value = "/ontologies", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<OntologyDto>> getMappingList(
//            @Parameter(description = "Collection to filter set of ontologies", example = "CoyPu")
//            @RequestParam(required = true) List<String> collection,
//            Pageable pageable
//    ) {
//
//        System.out.println("List of all ontologies within selected collection");
//
//
//        List<OntologyDto> ontologyList =  processedOntologyService.getMappingOntologies();
//
//        List<OntologyDto> ontologyDtoListFilterByCollection = new ArrayList<OntologyDto>();
//
//        /**
//         * Iterates through the collections and extracts only those onologies which belong to
//         * selected collection.
//         */
//
//        for(OntologyDto dto: ontologyList){
//
//            for(String s : collection) {
//
//                if (dto.getCollection().contains(s)) {
//
//                System.out.println("dto.getCollection(): " + dto.getCollection() + " dto.getUri(): " + dto.getUri());
//
//                ontologyDtoListFilterByCollection.add(dto);
//
//                }
//            }
//        }
//
//    return HttpUtils.ok(ontologyDtoListFilterByCollection);
//
//    }

//    @Operation(summary = "List mappings between a pair of ontologies")
//    @GetMapping(value = "/allmappings", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<MappingDto>> getMappings() {
//
//        System.setProperty("http.agent", "Chrome");
//
//        List<MappingDto> mappingDtoList = processedMappingService.getAllMappings();
//
//        List<MappingDto> mappingDtoListFillteredByMappingId = new ArrayList<>();
//
//        for(MappingDto mappingDto: mappingDtoList){
//
//        mappingDtoListFillteredByMappingId.add(mappingDto);
//
//    }
//
//    return HttpUtils.ok(mappingDtoListFillteredByMappingId);
//
//    }

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
    public ResponseEntity<List<MappingGropedBySourceOntologyDto>> getMappingsFilteredByCollectionNames(){

        List<MappingGropedBySourceOntologyDto> mappingFilteredByCollectionNameoList =
                processedMappingService.getAllMappingsGroupedBySourceOntology();

        return HttpUtils.ok(mappingFilteredByCollectionNameoList);
    }

}