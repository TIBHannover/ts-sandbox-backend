package eu.tib.ontologyhistory.model;

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

    private String axiomType;
    private String axiomValue;
    private String axiomURI;
}
