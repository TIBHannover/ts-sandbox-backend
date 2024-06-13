package eu.tib.ontologyhistory.dto.conto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

public record Timeline(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant firstCommitTime,
        String commitLabel,
        String message
) {
}
