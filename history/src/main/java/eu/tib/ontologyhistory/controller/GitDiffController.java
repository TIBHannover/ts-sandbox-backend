package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.service.GitDiffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
@RequestMapping("/api/git/")
public class GitDiffController {

    private final GitDiffService gitDiffService;

    @PostMapping
    @Operation(summary = "Create one object")
    public ResponseEntity<String> create(
            @Parameter(description = "Raw ontology URL", example = "https://raw.githubusercontent.com/OpenEnergyPlatform/ontology/dev/src/ontology/imports/iao-extracted.owl")
            @RequestParam String url
    ) {

        gitDiffService.create(url);

        return new ResponseEntity<>("Created", HttpStatus.OK);
    }
}
