//package eu.tib.ontologyhistory.controller;
//
//import com.fasterxml.jackson.annotation.JsonView;
//import eu.tib.ontologyhistory.dto.ontology.OntologyDto;
//import eu.tib.ontologyhistory.model.Diff;
//import eu.tib.ontologyhistory.service.OntologyService;
//import eu.tib.ontologyhistory.view.Views;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.Instant;
//import java.util.*;
//
//import lombok.val;
//
//
//@RestController
//@RequestMapping("/api/history/ontologies")
//@AllArgsConstructor
//public class OntologyController {
//
//    private final OntologyService ontologyService;
//
//    @GetMapping
//    @Operation(summary = "Get all ontologies full request (may be lagging)")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Found the ontologies"),
//            @ApiResponse(responseCode = "404", description = "No ontologies found", content = @Content)
//    })
//    @Parameter(
//            name = "view",
//            schema = @Schema(
//                    type = "string",
//                    defaultValue = "full",
//                    allowableValues = { "short", "full"}
//            )
//    )
//    public ResponseEntity<List<OntologyDto>> getOntologies(@RequestParam(required = false, defaultValue = "full") String view) {
//        val ontologies = ontologyService.findAll();
//
//        if (ontologies.isEmpty()) {
//            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
//        }
//
//        return new ResponseEntity<>(ontologies, HttpStatus.OK);
//    }
//
//    @GetMapping(value = "/{id}")
//    @Operation(summary = "Get ontology by id")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Found the ontology"),
//            @ApiResponse(responseCode = "404", description = "Ontology not found", content = @Content)
//    })
//    @Parameter(
//            name = "view",
//            schema = @Schema(
//                    type = "string",
//                    defaultValue = "short",
//                    allowableValues = { "short", "full"}
//            )
//    )
//    public ResponseEntity<OntologyDto> getOntology(
//            @PathVariable String id,
//            @RequestParam(required = false, defaultValue = "short") String view) {
//        val ontologyDto = ontologyService.findById(id);
//
//        if (ontologyDto == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        return new ResponseEntity<>(ontologyDto, HttpStatus.OK);
//    }
//
//    @PostMapping
//    @Operation(summary = "Add new ontology")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Ontology created"),
//            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
//    })
//    public ResponseEntity<String> addOntology(@RequestBody OntologyDto ontologyDTO) {
//        ontologyService.insert(ontologyDTO);
//        return new ResponseEntity<>("Ontology created successfully", HttpStatus.CREATED);
//    }
//
//    @PostMapping("/swagger")
//    @Operation(summary = "Add new ontology by URL link")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Ontology created"),
//            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
//    })
//    public ResponseEntity<OntologyDto> createOntology(
//            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/refs/heads/dev/src/ontology/imports/iao-extracted.owl")
//            @RequestParam String url) {
//        val ontologyDto = ontologyService.create(url);
//
//        if (ontologyDto == null) {
//            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//        }
//
//        return new ResponseEntity<>(ontologyDto, HttpStatus.CREATED);
//    }
//
//    @DeleteMapping
//    @Operation(summary = "Remove all ontologies")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "Ontologies deleted", content = @Content)
//    })
//    public ResponseEntity<String> deleteAllOntologies() {
//        val ontologies = ontologyService.findAll();
//        for (val ontology : ontologies) {
//            ontologyService.deleteById(ontology.id());
//        }
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
//
//    @DeleteMapping("/{id}")
//    @Operation(summary = "Remove ontology by id")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "Ontology deleted", content = @Content)
//    })
//    public ResponseEntity<String> deleteOntology(@PathVariable String id) {
//        ontologyService.deleteById(id);
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//    }
//
//    @PutMapping("/update/{id}")
//    @Operation(summary = "Update ontology by id")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Ontology updated"),
//            @ApiResponse(responseCode = "404", description = "Ontology not found", content = @Content)
//    })
//    public ResponseEntity<String> updateOntology(@PathVariable String id,
//                                                 @RequestBody OntologyDto ontologyDto) {
//        val ontology = ontologyService.findById(id);
//        if (ontology == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        ontology.diffs().addAll(0, ontologyDto.diffs());
//        ontologyService.update(id, ontology);
//        return new ResponseEntity<>("Ontology updated successfully", HttpStatus.OK);
//    }
//
//    @PutMapping("/edit/{id}")
//    @Operation(summary = "Edit ontology by id")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Ontology updated"),
//            @ApiResponse(responseCode = "404", description = "Ontology not found", content = @Content)
//    })
//    public ResponseEntity<String> editOntology(@PathVariable String id,
//                                               @JsonView(Views.Edit.class) @RequestBody OntologyDto ontologyDto) {
//
//        val ontology = ontologyService.findById(id);
//        if (ontology == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        ontologyService.edit(id, ontologyDto);
//        return new ResponseEntity<>("Ontology updated successfully", HttpStatus.OK);
//    }
//
//    @PostMapping("/{id}/preview")
//    @Operation(summary = "Get ontology diffs between two dates")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Found the diffs"),
//            @ApiResponse(responseCode = "204", description = "No diffs found", content = @Content),
//            @ApiResponse(responseCode = "404", description = "Ontology not found", content = @Content)
//    })
//    public ResponseEntity<List<Diff>> getPreview(@PathVariable String id,
//                                                 @RequestParam Instant startDate,
//                                                 @RequestParam Instant endDate) {
//        val diffs = ontologyService.getDiffsBetween(id, startDate, endDate);
//        if (diffs == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        if (diffs.isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        }
//
//        return new ResponseEntity<>(diffs, HttpStatus.OK);
//    }
//
//}
