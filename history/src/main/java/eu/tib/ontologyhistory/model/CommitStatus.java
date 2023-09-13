package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize()
public class CommitStatus {
    @JsonView
    private String status;
    @JsonView
    private String commitsBehind;
    @JsonView
    private String branch;
}
