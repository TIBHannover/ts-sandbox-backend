package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.OntologyGitDiffRequest;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.exception.UnloadableCustomImportException;
import eu.tib.ontologyhistory.model.exception.UnparsableCustomOntologyException;
import eu.tib.ontologyhistory.service.DiffService;
import eu.tib.ontologyhistory.utils.ExceptionUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import java.util.*;

@RestController
@RequestMapping("/api/history/diff")
public class DiffController {

    private final DiffService diffService;

    @Inject
    private RequestDetails requestDetails;

    public DiffController(DiffService diffService) {
        this.diffService = diffService;
    }

    @GetMapping
    @Operation(summary = "Get all diffs")
    public List<Diff> getDiffs() {
        return diffService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get diff by id")
    public ResponseEntity<Diff> getDiff(@PathVariable String id) {
        Diff diff = diffService.findById(id);
        if (diff == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(diff, HttpStatus.OK);
    }

    @PostMapping(value = "/add")
    public ResponseEntity<Diff> addDiffGit(
            @RequestBody OntologyGitDiffRequest ontologyGitDiffRequest, WebRequest webRequest) throws Exception {
        Diff diff;
            try {
                diff = diffService.makeDiffFromGit(ontologyGitDiffRequest.getGitUrlLeft(),
                        ontologyGitDiffRequest.getGitUrlRight(),
                        ontologyGitDiffRequest.getSha(),
                        ontologyGitDiffRequest.getParentSha(),
                        ontologyGitDiffRequest.getShaOffsetDateTime(),
                        ontologyGitDiffRequest.getParentOffsetDateTime(),
                        ontologyGitDiffRequest.getCommitDate(),
                        ontologyGitDiffRequest.getMessage());
            } catch (Exception e) {
                requestDetails.setRequestBody(ontologyGitDiffRequest);
                webRequest.setAttribute("requestBody", ontologyGitDiffRequest, RequestAttributes.SCOPE_REQUEST);
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
    public ResponseEntity<Diff> deleteDiff(@PathVariable String id) {
        diffService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
