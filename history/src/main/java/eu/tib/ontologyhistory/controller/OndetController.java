package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.service.OndetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ondet/sdiffs")
@AllArgsConstructor
public class OndetController {

    private final OndetService ondetService;

    @GetMapping
    @Operation(summary = "Find all objects")
    public ResponseEntity<List> findAll(
            @Parameter(description = "Apache Jena Dataset")
            @RequestParam String dataset
    ) {
        val objects = ondetService.findAll(dataset);

        return new ResponseEntity<>(objects, HttpStatus.OK);
    }

    @GetMapping("/{sha}")
    @Operation(summary = "Find one object by sha")
    public ResponseEntity<Object> find(
            @PathVariable String sha,

            @Parameter(description = "Apache Jena Dataset")
            @RequestParam String dataset
    ) {

        val diff = ondetService.find(sha, dataset);

        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create one object")
    public ResponseEntity<String> create(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/dev/src/ontology/imports/iao-extracted.owl")
            @RequestParam String url,

            @Parameter(description = "Apache Jena Dataset")
            @RequestParam String dataset
    ) {

        val result = ondetService.findByUrl(url);

        if (!result.isEmpty()) {
            return new ResponseEntity<>("Already exists in the database", HttpStatus.FOUND);
        }

        ondetService.create(url, dataset);
        
        return new ResponseEntity<>("Created", HttpStatus.OK);
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

    
}
