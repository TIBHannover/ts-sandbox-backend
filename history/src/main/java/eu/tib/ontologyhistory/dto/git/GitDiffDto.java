package eu.tib.ontologyhistory.dto.git;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record GitDiffDto(
        String id,

        String url,

        String sha,

        String parentSha,

        String diff,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant datetime
) {

    public static GitDiffDto defaultValue() {
        return new GitDiffDto("", "", "", "", "", Instant.now());
    }
}
