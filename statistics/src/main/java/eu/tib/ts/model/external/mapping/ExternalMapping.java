package eu.tib.ts.model.external.mapping;

import eu.tib.ts.controller.dto.TargetOntologyObjectSetModel;
import eu.tib.ts.model.ontology.Ontology;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class ExternalMapping {

    long id;

    String mappingId;

    Ontology sourceOntology;

    int numberOfTargetOntologies;

    Set<TargetOntologyObjectSetModel> targetOntologyList;

}