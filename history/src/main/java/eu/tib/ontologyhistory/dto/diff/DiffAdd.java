package eu.tib.ontologyhistory.dto.diff;

import java.time.Instant;


public record DiffAdd (
        String gitUrlLeft,

        String gitUrlRight,

        String gitRawFileLeft,

        String gitRawFileRight,

        String sha,

        String parentSha,

        Instant shaOffsetDateTime,

        Instant parentOffsetDateTime,

        Instant commitDate,

        String message
) {
    public static final int MAX_MARKDOWN_LENGTH = 300;
}
