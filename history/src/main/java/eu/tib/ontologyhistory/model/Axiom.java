package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;
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

    @JsonView(Views.Full.class)
    @Schema(description = "Axiom type")
    private String axiomType;

    @JsonView(Views.Full.class)
    @Schema(description = "Axiom value")
    private String axiomValue;

    @JsonView(Views.Full.class)
    @Schema(description = "Axiom URI")
    private String axiomURI;
}
