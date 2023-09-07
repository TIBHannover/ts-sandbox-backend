package eu.tib.ontologyhistory.dto;

import eu.tib.ontologyhistory.model.Ontology;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LeftRightOntologies {
    private final Ontology left;
    private final Ontology right;

}
