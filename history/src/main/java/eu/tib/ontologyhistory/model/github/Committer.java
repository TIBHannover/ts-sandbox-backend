package eu.tib.ontologyhistory.model.github;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Committer(
        String name,

        @JsonView(Views.Short.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant date
) {}
