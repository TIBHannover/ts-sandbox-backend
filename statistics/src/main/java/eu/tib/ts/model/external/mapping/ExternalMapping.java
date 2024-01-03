package eu.tib.ts.model.external.mapping;

import eu.tib.ts.controller.dto.TargetOntologyObjectSetModel;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class ExternalMapping {

    String mappingId;
    String sourceOntologyURI;
    int numberOfTargetOntologies;
    Set<TargetOntologyObjectSetModel> targetOntologyList;

}