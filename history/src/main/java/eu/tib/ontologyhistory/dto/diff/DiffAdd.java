package eu.tib.ontologyhistory.dto.diff;

import java.time.Instant;


public record DiffAdd (
        String gitUrlLeft,

        String gitUrlRight,

        String gitRawFileLeft,

        String gitRawFileRight,

        String sha,

        String parentSha,

        Instant parentOffsetDateTime,

        Instant shaOffsetDateTime,

        Instant commitDate,

        String message
) {
    public static final int MAX_MARKDOWN_LENGTH = 300;
}
