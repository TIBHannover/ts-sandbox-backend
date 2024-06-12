package eu.tib.ontologyhistory.dto.conto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record TimelineMessage(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant commitTime,
        String label,
        String predicate,
        String object
) {
}
