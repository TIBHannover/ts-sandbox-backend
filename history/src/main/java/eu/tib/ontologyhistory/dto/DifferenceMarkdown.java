package eu.tib.ontologyhistory.dto;

import eu.tib.ontologyhistory.dto.conto.Difference;
import io.swagger.v3.oas.annotations.media.Schema;
import org.bson.Document;

public record DifferenceMarkdown(
        @Schema(description = "Markdown diff content", implementation = java.lang.String.class)
        Document markdown,

        Difference difference,

        String gitDiff
) {
}
