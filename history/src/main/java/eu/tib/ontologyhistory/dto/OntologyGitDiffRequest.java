package eu.tib.ontologyhistory.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class OntologyGitDiffRequest {

    private String gitUrlLeft;


    private String gitUrlRight;


    private String sha;


    private String parentSha;

    private String commitDate;


    private String message;
}
