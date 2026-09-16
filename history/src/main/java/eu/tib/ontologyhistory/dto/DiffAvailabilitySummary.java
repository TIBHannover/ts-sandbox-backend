package eu.tib.ontologyhistory.dto;

public record DiffAvailabilitySummary(
        DiffAvailability robot,
        DiffAvailability conto,
        DiffAvailability git
) {
}
