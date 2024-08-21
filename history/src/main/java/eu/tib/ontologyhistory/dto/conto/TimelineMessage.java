package eu.tib.ontologyhistory.dto.conto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record TimelineMessage(
        String label,
        String predicate,
        String object
) {
}
