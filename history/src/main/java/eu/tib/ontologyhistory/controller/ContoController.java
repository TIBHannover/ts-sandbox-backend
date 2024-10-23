package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.conto.Difference;
import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.conto.Timeline;
import eu.tib.ontologyhistory.dto.conto.TimelineMessage;
import eu.tib.ontologyhistory.model.InvalidContoDiff;
import eu.tib.ontologyhistory.repository.InvalidContoDiffRepository;
import eu.tib.ontologyhistory.service.ContoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;

import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/history/conto")
public class ContoController {

    private final InvalidContoDiffRepository invalidContoDiffRepository;
    private ContoService contoService;

    private static final String DATASET = "test";

    @PostMapping("/add")
    @Operation(summary = "Calculate and save into triple story Semantic Diffs with Conto Diff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ontology created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<List<String>> createOntology(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/refs/heads/dev/src/ontology/imports/iao-extracted.owl")
            @RequestParam String url
            ) {

        contoService.create(url, DATASET);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all ontologies from the triple store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the ontologies"),
            @ApiResponse(responseCode = "404", description = "No ontologies found", content = @Content)
    })
    public ResponseEntity<Set<TempGraph>> getOntologies(
    ) {
        val ontologies = contoService.findAll(DATASET);

        if (ontologies.isEmpty()) {
            return new ResponseEntity<>(Collections.emptySet(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ontologies, HttpStatus.OK);
    }

    @GetMapping("/findAllInvalid")
    @Operation(summary = "Find all invalid conto diffs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the ontologies"),
            @ApiResponse(responseCode = "404", description = "No ontologies found", content = @Content)
    })
    public ResponseEntity<List<InvalidContoDiff>> findAllInvalidContoDiffs(
    ) {
        val ontologies = invalidContoDiffRepository.findAll();

        return new ResponseEntity<>(ontologies, HttpStatus.OK);
    }

    @GetMapping("/timeline")
    @Operation(summary = "Get timeline for the ontology")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the timeline"),
            @ApiResponse(responseCode = "404", description = "No timeline found", content = @Content)
    })
    public ResponseEntity<List<Timeline>> getTimeline(
            @Parameter(description = "ontologyUrl")
            @RequestParam String ontologyUrl
            ) {

        val ontologies = contoService.findByUrl(ontologyUrl, DATASET);

        return new ResponseEntity<>(ontologies, HttpStatus.OK);
    }

    @GetMapping("/timelineElem")
    @Operation(summary = "Get timeline for the ontology")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the timeline"),
            @ApiResponse(responseCode = "404", description = "No timeline found", content = @Content)
    })
    public ResponseEntity<Difference> getTimelineElement(
            @Parameter(description = "commitId")
            @RequestParam String commitId
            ) {

        val ontologies = contoService.timeline(commitId, DATASET);

        return new ResponseEntity<>(ontologies, HttpStatus.OK);
    }

    @PostMapping("/upload")
    @Operation(summary = "Save data into the Apache Fuseki triple store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ontology created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<String> uploadOntlogy(
            @Parameter(description = "Ontology as a string", example = "https://www.w3.org/1999/02/22-rdf-syntax-ns#")
            @RequestParam String ontology
            ) {

        contoService.uploadOntologyToFuseki(ontology, DATASET);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/operationsData")
    @Operation(summary = "Get operations data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the operations data"),
            @ApiResponse(responseCode = "404", description = "No operations data found", content = @Content)
    })
    public ResponseEntity<List<String>> getOperationsData(
            @Parameter(description = "ontologyUrl")
            @RequestParam String ontologyUrl) {

        val subjects = contoService.operationsData(DATASET, ontologyUrl);

        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @GetMapping("/versions")
    @Operation(summary = "Get versions data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the versions data"),
            @ApiResponse(responseCode = "404", description = "No versions data found", content = @Content)
    })
    public ResponseEntity<List<Timeline>> getVersions(
            @Parameter(description = "ontologyUrl")
            @RequestParam String ontologyUrl) {

        val subjects = contoService.getVersions(DATASET, ontologyUrl);

        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @GetMapping("/versionsElem")
    @Operation(summary = "Get versions data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the versions data"),
            @ApiResponse(responseCode = "404", description = "No versions data found", content = @Content)
    })
    public ResponseEntity<Map<Instant, Collection<TimelineMessage>>> getVersionElem(
            @Parameter(description = "ontologyUrl")
            @RequestParam String ontologyUrl,

            @Parameter(description = "resourceUri")
            @RequestParam String resourceUri,

            @Parameter(description = "firstDate")
            @RequestParam String firstDate,

            @Parameter(description = "secondDate")
            @RequestParam String secondDate) {

        val subjects = contoService.timelineMessage(DATASET, ontologyUrl, resourceUri, firstDate, secondDate);

        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }


    @GetMapping("/report")
    @Operation(summary = "Get data in a period of time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the data"),
            @ApiResponse(responseCode = "404", description = "No data found", content = @Content)
    })
    public ResponseEntity<List<String>> createReport(
            @Parameter(description = "ontologyUrl")
            @RequestParam String ontologyUrl,

            @Parameter(description = "firstDate")
            @RequestParam String firstDate,

            @Parameter(description = "secondDate")
            @RequestParam String secondDate) {

        val subjects = contoService.dataInBetweenDates(DATASET, ontologyUrl, firstDate, secondDate);

        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

}
