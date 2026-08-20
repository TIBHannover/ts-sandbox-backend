package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.net.URI;
import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
public class BatchOntologyResult {

    private URI uri;

    private BatchOntologyStatus status;

    private String message;

    private Integer diffPairCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant finishedAt;
}
