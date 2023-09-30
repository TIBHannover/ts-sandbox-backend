package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;

import eu.tib.ts.controller.dto.SourceOntologyObjectSetModel;
import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.service.MappingFilterService;
import eu.tib.ts.service.MappingService;
import eu.tib.ts.service.ProcessedMappingService;
import eu.tib.ts.service.impl.MappingServiceImpl;
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
import java.util.Set;

@RestController
@RequestMapping("api/ontology")
public class MappingController {

    private final ProcessedMappingService processedMappingService;

    private final MappingFilterService mappingFilterService;

    private final MappingServiceImpl mappingService;

    @Autowired
    public MappingController(
            ProcessedMappingService processedMappingService,
            MappingFilterService mappingFilterService,
            MappingServiceImpl mappingService) {

        this.processedMappingService = processedMappingService;
        this.mappingFilterService = mappingFilterService;
        this.mappingService = mappingService;
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

    @Operation(summary="Filter mappings by selected one or more ontology collection")
    @GetMapping(value="/gourpedmappings/filterby", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MappingGropedBySourceOntologyDto>> getMappingsFilteredByCollectionNames(
            @Parameter(description = "Filter set of mappings for source ontologies that belong " +
                    "to given collections", example = "NFDI4ING")
            @RequestParam List<String> collection,
            Pageable pageable
    ){

        List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoList =
                processedMappingService.getAllMappingsGroupedBySourceOntology();

        List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoListFillteredByMappingId = new ArrayList<>();

        for(MappingGropedBySourceOntologyDto mappingGroupedBySourceOntologyDto: mappingGropedBySourceOntologyDtoList){

            Set<SourceOntologyObjectSetModel> sourceOntologyObjectSetModels = mappingGroupedBySourceOntologyDto.getSourceOntology();

            for(SourceOntologyObjectSetModel sm: sourceOntologyObjectSetModels){

                Set<String> allCollections = sm.getCollection();

                if(containsCollection(allCollections,collection)){

                    mappingGropedBySourceOntologyDtoListFillteredByMappingId.add(mappingGroupedBySourceOntologyDto);
                }

                }

            }

        return HttpUtils.ok(mappingGropedBySourceOntologyDtoListFillteredByMappingId);
    }

    /**
     * 
     * Returns true if collection names from from parameter list match
     * collection names in source ontology. Otherwise returns false.
     *
     * @param allCollections
     * @param selectedCollection
     * @return
     */
    public boolean containsCollection(Set<String> allCollections, List<String> selectedCollection){

        boolean equalStrings = false;

        for(String s1: allCollections ){

            for(String s2: selectedCollection){

                if(s1.equals(s2)){

                    System.out.println("all collections: " + s1 + " collection: " + s2);

                    equalStrings = true;

                    return equalStrings;

                }
            }
        }

       return equalStrings;
    }
}