package eu.tib.ontologyhistory.dto.conto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Difference(
    List<String> changes,

    String error
) {
}
