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

import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

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

//        LogMap2_Matcher logmap2 = new LogMap2_Matcher("https://gitlab.com/coypu-project/coy-ontology/-/raw/main/ontology/global/coy.ttl",
//                "https://gitlab.isl.ics.forth.gr/cidoc-crm/cidoc_crm_rdf/-/raw/master/7.1.2/CIDOC_CRM_v7.1.2.rdf");
//
//        Set<MappingObjectStr> logmap2_mappings = logmap2.getLogmap2_Mappings();
//
//        System.out.println("Number of mappings computed by LogMap: " + logmap2_mappings.size());
//
//        for(MappingObjectStr mos: logmap2_mappings){
//
//            if(mos.getTypeOfMapping()==MappingObjectStr.CLASSES) {
//
//                System.out.println(mos.getIRIStrEnt1() + " , " + mos.getMappingDirection() + " , " +
//                        mos.getIRIStrEnt2() + " , " + mos.getTypeOfMapping() + " , " +
//                        mos.getStructuralConfidenceMapping());
//
//            }
//        }


    return HttpUtils.ok(ontologyDtoListFilterByCollection);

    }
}