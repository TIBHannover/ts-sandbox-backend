package eu.tib.ontologyhistory.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Commit(
        String url,
        String sha,
        CommitDetail commit,
        List<ParentCommit> parents

) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitDetail(
            Committer committer,
            String message
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Committer(
                String name,
                Instant date
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParentCommit (
            String url,
            String sha
    ) {}
}



