package eu.tib.ontologyhistory.dto.diff;

import java.time.Instant;


public record DiffAdd (
        String gitUrlLeft,

        String gitUrlRight,

        String gitCommitUrlLeft,

        String gitCommitUrlRight,

        String gitRawFileLeft,

        String gitRawFileRight,

        String sha,

        String parentSha,

        Instant datetime,

        Instant parentDatetime,

        String messageLeft,

        String messageRight
) {
    public static final int MAX_MARKDOWN_LENGTH = 300;
}
