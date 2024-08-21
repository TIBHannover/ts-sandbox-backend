package eu.tib.ontologyhistory.controller;

import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.conto.GraphInfo;
import eu.tib.ontologyhistory.dto.conto.Timeline;
import eu.tib.ontologyhistory.service.OndetService;
import eu.tib.ontologyhistory.view.Views;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/ondet/sdiffs")
@AllArgsConstructor
public class OndetController {

    private final OndetService ondetService;

    private static final String DATASET = "test";

    @GetMapping
    @Operation(summary = "Find all objects")
    public ResponseEntity<Set<GraphInfo>> findAll(
    ) {
        val objects = ondetService.findAll(DATASET);

        return new ResponseEntity<>(objects, HttpStatus.OK);
    }

    @GetMapping("/{sha}")
    @Operation(summary = "Find one object by sha")
    public ResponseEntity<DifferenceMarkdown> find(
            @PathVariable String sha
    ) {

        val diff = ondetService.find(sha, DATASET);

        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @GetMapping("/checkUrl")
    @Operation(summary = "Check if object exists by URL")
    public ResponseEntity<String> findByUrl(@RequestParam String url,
                                            HttpServletRequest httpServletRequest) {

        val diff = ondetService.findFirstByUrl(url, DATASET);

        if (diff.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        val responseUrl = httpServletRequest.getRequestURL().append('?').append(url).toString();

        return new ResponseEntity<>(responseUrl, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create one object")
    public ResponseEntity<String> create(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/dev/src/ontology/imports/iao-extracted.owl")
            @RequestParam String url,
            HttpServletRequest httpServletRequest
    ) {

        val result = ondetService.create(url, DATASET);

        if (result.isEmpty()) {
            return new ResponseEntity<>("Your provided ontology was not added into the system, since all semantic diffs failed", HttpStatus.NOT_FOUND);
        }

        val responseUrl = httpServletRequest.getRequestURL().append('?').append(url).toString();

        return new ResponseEntity<>(responseUrl, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove one object")
    public ResponseEntity<String> removeById(
            @PathVariable String id
    ) {
        ondetService.remove(id);

        return new ResponseEntity<>("Removed one", HttpStatus.OK);
    }

    @DeleteMapping
    @Operation(summary = "Remove all objects")
    public ResponseEntity<String> removeAll() {
        ondetService.removeAll();

        return new ResponseEntity<>("Removed all", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one object")
    public ResponseEntity<String> update(
            @PathVariable String id
    ) {

        ondetService.update(id);

        return new ResponseEntity<>("Updated", HttpStatus.OK);
    }

    @GetMapping("/commits")
    @Operation(summary = "Get timeline for the ontology")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the timeline"),
            @ApiResponse(responseCode = "404", description = "No timeline found", content = @Content)
    })
    @JsonView(Views.Short.class)
    public ResponseEntity<List<?>> getCommits(
            @Parameter(description = "ontologyUrl")
            @RequestParam String ontologyUrl
    ) {

        val commits = ondetService.getCommits(ontologyUrl);

        return new ResponseEntity<>(commits, HttpStatus.OK);
    }
}
