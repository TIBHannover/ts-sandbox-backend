package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.diff.DiffAndApiError;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.service.DiffService;
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
public class DiffController {

    private final DiffService diffService;

    @GetMapping
    @Operation(summary = "Get all diffs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the diffs"),
            @ApiResponse(responseCode = "404", description = "No diffs found", content = @Content)
    })
    public ResponseEntity<List<DiffDto>> getDiffs() {
        val diffs = diffService.findAll();

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
    public ResponseEntity<DiffDto> getDiff(@PathVariable String id) {
        val diff = diffService.findById(id);

        if (diff == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @PostMapping(value = "/add")
    @Operation(summary = "Add new diff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Diff created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<DiffDto> addDiffGit(@RequestBody DiffAdd diffAdd) {
        val diff = diffService.makeDiffFromGit(diffAdd);

        if (diff == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(diff, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}") 
    @Operation(summary = "Remove diff by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ontology deleted", content = @Content)
    })
    public ResponseEntity<String> deleteDiff(@PathVariable String id) {
        diffService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/external")
    @Operation(summary = "Find diffs since datetime")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Diffs are found"),
            @ApiResponse(responseCode = "204", description = "No diffs since provided datetime", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<DiffAndApiError> external(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/tibonto/dr/master/DigitalReference.ttl")
            @RequestParam String ontologyURL,
            @Parameter(description = "ISO 8601 datetime", example = "2024-01-20T16:00:49Z")
            @RequestParam Instant startSha) {

        val diffs = diffService.makeDiffExternal(ontologyURL, startSha);

        if (diffs.diffs().isEmpty() && diffs.apiErrors().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(diffs, HttpStatus.OK);
    }

}
