package eu.tib.ontologyhistory.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class OntologyGitDiffRequest {

    private String gitUrlLeft;

    private String gitUrlRight;

    private String gitRawFileLeft;

    private String gitRawFileRight;

    private String sha;

    private String parentSha;

    private Instant parentOffsetDateTime;

    private Instant shaOffsetDateTime;
    
    private Instant commitDate;

    private String message;
}
