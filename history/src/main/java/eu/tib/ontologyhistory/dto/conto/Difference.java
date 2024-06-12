package eu.tib.ontologyhistory.dto.conto;

import java.util.List;

public record Difference(
    List<String> changes
) {
}
