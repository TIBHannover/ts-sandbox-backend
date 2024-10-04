package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.service.RobotService;
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
@RequestMapping("/api/history/diff")
public class RobotController {

    private final RobotService robotService;

    @GetMapping
    @Operation(summary = "Get all diffs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the diffs"),
            @ApiResponse(responseCode = "404", description = "No diffs found", content = @Content)
    })
    public ResponseEntity<List<DiffDto>> getAll() {
        val diffs = robotService.findAll();

        if (diffs.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(diffs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get diff by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the diff"),
            @ApiResponse(responseCode = "404", description = "Diff not found", content = @Content)
    })
    public ResponseEntity<DiffDto> getById(@PathVariable String id) {
        val diff = robotService.findById(id);

        if (diff == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @GetMapping("/getBySha")
    @Operation(summary = "Get diff by unique SHA-1 commit number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the diff"),
            @ApiResponse(responseCode = "404", description = "Diff not found", content = @Content)
    })
    public ResponseEntity<DiffDto> getBySha(
            @Parameter(description = "Sha of the searched diff")
            @RequestParam String sha) {

        val diff = robotService.findBySha(sha);

        if (diff == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @GetMapping("/getAllByUrl")
    @Operation(summary = "Get list of diffs by ontology URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the diff"),
            @ApiResponse(responseCode = "404", description = "Diff not found", content = @Content)
    })
    public ResponseEntity<List<DiffDto>> getAllByUrl(
            @Parameter(description = "Raw URL of the ontology used in a calculation of the Robot Diffs")
            @RequestParam String url) {

        val diffs = robotService.findAllByUrl(url);

        if (diffs.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(diffs, HttpStatus.OK);
    }

    @PostMapping("/add")
    @Operation(summary = "Add new ontology by URL link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Diff created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<String> add(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/refs/heads/dev/src/ontology/imports/iao-extracted.owl")
            @RequestParam String url) {

        robotService.create(url);

        return new ResponseEntity<>("Created", HttpStatus.CREATED);
    }

    @DeleteMapping
    @Operation(summary = "Remove all robot diffs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Robot diffs deleted", content = @Content)
    })
    public ResponseEntity<String> deleteAll() {

        robotService.deleteAll();

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove diff by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ontology deleted", content = @Content)
    })
    public ResponseEntity<String> deleteById(@PathVariable String id) {

        robotService.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/deleteAllByUrl")
    @Operation(summary = "Remove all robot diffs calculated for provided ontology raw URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Robot diffs deleted", content = @Content)
    })
    public ResponseEntity<String> deleteAllByUrl(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/refs/heads/dev/src/ontology/imports/iao-extracted.owl")
            @RequestParam String url
    ) {

        robotService.deleteAllByUrl(url);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
