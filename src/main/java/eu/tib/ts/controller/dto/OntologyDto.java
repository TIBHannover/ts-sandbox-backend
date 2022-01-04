package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedOntology;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class OntologyDto {
    String ontologyId;
    String uri;

    public static OntologyDto of(ProcessedOntology processedOntology) {
        return OntologyDto.builder()
            .ontologyId(processedOntology.getOntologyId())
            .uri(processedOntology.getUri())
            .build();
    }
}
