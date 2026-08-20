package eu.tib.ontologyhistory.dto.git;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.net.URI;
import java.time.Instant;

public record GitDiffDto(
        String id,

        URI uri,

        String sha,

        String parentSha,

        String diff,

        String rawFileUrl,

        String repositoryFileUrl,

        String repositoryTreeUrl,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant datetime
) {
}
