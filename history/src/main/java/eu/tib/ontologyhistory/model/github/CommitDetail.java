package eu.tib.ontologyhistory.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommitDetail(

        @JsonView(Views.Short.class)
        Committer committer,

        @JsonView(Views.Short.class)
        String message
) {}
