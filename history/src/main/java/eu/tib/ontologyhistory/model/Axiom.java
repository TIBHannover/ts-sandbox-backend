package eu.tib.ontologyhistory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@Setter
@Getter
@Builder
@Jacksonized
public class Axiom {

    @Schema(description = "Axiom type")
    private String axiomType;

    @Schema(description = "Axiom value")
    private String axiomValue;

    @Schema(description = "Axiom URI")
    private String axiomURI;
}
