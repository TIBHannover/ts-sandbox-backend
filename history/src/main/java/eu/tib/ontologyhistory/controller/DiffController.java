package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.diff.DiffAndApiError;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.exception.UnloadableCustomImportException;
import eu.tib.ontologyhistory.model.exception.UnparsableCustomOntologyException;
import eu.tib.ontologyhistory.service.DiffService;
import eu.tib.ontologyhistory.utils.ExceptionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.val;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/history/diff")
public class DiffController {

    private final DiffService diffService;

    @GetMapping
    @Operation(summary = "Get all diffs")
    public List<DiffDto> getDiffs() {
        return diffService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get diff by id")
    public ResponseEntity<DiffDto> getDiff(@PathVariable String id) {
        val diff = diffService.findById(id);
        if (diff == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @PostMapping(value = "/add")
    public ResponseEntity<Diff> addDiffGit(
            @RequestBody DiffAdd diffAdd, WebRequest webRequest) {
        Diff diff;
            try {
                diff = diffService.makeDiffFromGit(diffAdd);
            } catch (Exception e) {
                webRequest.setAttribute("requestBody", diffAdd, RequestAttributes.SCOPE_REQUEST);
                Throwable throwable = ExceptionUtils.findRootCause(e);
                if (throwable instanceof UnloadableImportException) {
                    throw new UnloadableCustomImportException(throwable.getMessage());
                } else if (throwable instanceof UnparsableOntologyException) {
                    throw new UnparsableCustomOntologyException(throwable.getMessage());
                } else {
                    throw e;
                }
            }
        return ResponseEntity.ok().body(diff);
    }

    @DeleteMapping("/{id}") 
    @Operation(summary = "Remove diff by id")
    public ResponseEntity<String> deleteDiff(@PathVariable String id) {
        diffService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/external")
    public ResponseEntity<DiffAndApiError> external(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/tibonto/dr/master/DigitalReference.ttl")
            @RequestParam String ontologyURL,
            @Parameter(description = "ISO 8601 datetime", example = "2024-01-20T16:00:49Z")
            @RequestParam Instant startSha) {

        val diffs = diffService.makeDiffExternal(ontologyURL, startSha);

        return new ResponseEntity<>(diffs, HttpStatus.OK);
    }

}
