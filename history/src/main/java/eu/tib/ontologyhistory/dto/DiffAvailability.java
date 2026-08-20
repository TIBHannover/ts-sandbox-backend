package eu.tib.ontologyhistory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiffAvailability(
        DiffAvailabilityStatus status,
        String message,
        String url,
        Integer sizeBytes,
        Boolean inlineRecommended
) {
}
