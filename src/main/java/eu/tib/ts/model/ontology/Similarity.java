package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.OntologyDto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Similarity {
    String name;
    List<OntologyDto> ontologies;
}
