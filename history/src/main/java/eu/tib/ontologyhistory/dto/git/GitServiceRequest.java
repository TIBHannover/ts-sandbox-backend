package eu.tib.ontologyhistory.dto.git;

import lombok.Builder;

import java.time.Instant;

@Builder
public record GitServiceRequest(
    String projectId,

    String owner,

    String repo,

    String path,

    String branch,

    Instant datetime
) {
}
