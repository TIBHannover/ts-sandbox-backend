package eu.tib.ontologyhistory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.view.Views;

import java.net.URI;
import java.time.Instant;

public record ProcessedDiffTimelineItem(
        @JsonView(Views.Short.class)
        URI uri,

        @JsonView(Views.Short.class)
        String sha,

        @JsonView(Views.Short.class)
        String parentSha,

        @JsonView(Views.Short.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant date,

        @JsonView(Views.Short.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant parentDate,

        @JsonView(Views.Short.class)
        String message,

        @JsonView(Views.Short.class)
        String robotStatus,

        @JsonView(Views.Short.class)
        String robotErrorCode,

        @JsonView(Views.Short.class)
        boolean gitDiffAvailable
) implements Commit {

    @Override
    public Instant getDatetime() {
        return date;
    }
}
