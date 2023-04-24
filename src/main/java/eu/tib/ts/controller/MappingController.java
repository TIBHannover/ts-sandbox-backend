package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.OntologyDto;

import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.service.ProcessedMappingService;
import eu.tib.ts.service.ProcessedOntologyService;
import eu.tib.ts.utils.HttpUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @ApiOperation("List of all ontologies within selected collection")
    @GetMapping(value = "/ontologies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OntologyDto>> getMappingList(
            @ApiParam(value = "Collection to filter set of ontologies", example = "CoyPu")
            @RequestParam(required = true) List<String> collection,
            Pageable pageable
    ) {

        System.out.println("List of all ontologies within selected collection");


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

    @ApiOperation("List of all mappings between ontologies")
    @GetMapping(value = "/mappings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MappingDto>> getMappings(
            Pageable pageable
    ) {

        System.out.println("Mappings between ontologies");

        List<MappingDto> mappingDtoList = processedMappingService.getMappings();

        List<MappingDto> mappingDtoListFillteredByMappingId = new ArrayList<MappingDto>();

        for(MappingDto mappingDto: mappingDtoList){


            mappingDtoListFillteredByMappingId.add(mappingDto);

            System.out.println(mappingDto.getMappingId());
            System.out.println(mappingDto.getSourceOntology()+ " " + mappingDto.getTargetOntology());

            for(MappingObjectStr mostr: mappingDto.getMappingObjectStrs()){

                System.out.println(mostr.getIRIStrEnt1() +" - " + mostr.getMappingDirection() + " - "+ mostr.getIRIStrEnt2()+" - " + mostr.getTypeOfMapping());
            }
        }


        return HttpUtils.ok(mappingDtoListFillteredByMappingId);

    }

}