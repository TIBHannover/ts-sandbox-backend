package eu.tib.ontologyhistory.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
public class InvalidContoDiff {

    @Id
    private String id;

    private String uri;

    private String sha;

    private String parentSha;

    private String stage;

    private String message;

    private Long outputSizeBytes;

    private Long quadSizeBytes;

    private Instant createdAt;
}
