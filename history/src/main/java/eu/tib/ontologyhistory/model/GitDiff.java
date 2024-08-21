package eu.tib.ontologyhistory.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@Jacksonized
public class GitDiff {

    @Id
    private String id;

    private String url;

    private String sha;

    private String parentSha;

    private String diff;
}
