package eu.tib.ontologyhistory.dto;

import eu.tib.ontologyhistory.model.ApiError;
import eu.tib.ontologyhistory.model.CommitStatus;
import eu.tib.ontologyhistory.model.Diff;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class OntologyDTO {
    private String url;

    private String name;

    private String description;

    private List<Diff> diffs;

    private List<ApiError> invalidDiffs;

    private CommitStatus commitStatus;

    private String type;

    private Instant atime;
}
