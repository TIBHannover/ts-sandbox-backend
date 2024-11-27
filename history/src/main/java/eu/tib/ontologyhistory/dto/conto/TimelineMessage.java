package eu.tib.ontologyhistory.dto.conto;

public record TimelineMessage(
        String label,
        String predicate,
        String object
) {
}
