package eu.tib.ontologyhistory.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Commit(
        String url,

        String html_url,

        @JsonView(Views.Short.class)
        String sha,

        @JsonView(Views.Short.class)
        CommitDetail commit,

        @JsonView(Views.Short.class)
        List<ParentCommit> parents

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParentCommit (
            String url,

            @JsonView(Views.Short.class)
            String sha
    ) {}
}



