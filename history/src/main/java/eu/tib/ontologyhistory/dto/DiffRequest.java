package eu.tib.ontologyhistory.dto;

import lombok.Getter;

@Getter
public class DiffRequest {

    private String ontProperty;
    private String firstCommit;
    private String secondCommit;
    private String id;
}
