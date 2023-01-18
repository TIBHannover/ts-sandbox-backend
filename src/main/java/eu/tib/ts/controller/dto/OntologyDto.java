package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedOntology;
import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;

@Builder
@Value
public class OntologyDto {

    long id;
    String ontologyId;
    String uri;
    String title;

    public static OntologyDto of(ProcessedOntology processedOntology) {

        return OntologyDto.builder()
                .id(processedOntology.getId())
                .ontologyId(processedOntology.getOntologyId())
                .uri(processedOntology.getUri())
                .title(processedOntology.getTitle())
                .build();

    }
}
