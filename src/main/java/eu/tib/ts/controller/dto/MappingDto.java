package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedOntology;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class MappingDto {

    long id;
    String mappingId;
    String uri;
    String title;

    public static MappingDto of(ProcessedOntology processedOntology) {

        return MappingDto.builder()
                .id(processedOntology.getId())
                .mappingId(processedOntology.getOntologyId())
                .uri(processedOntology.getUri())
                .title(processedOntology.getTitle())
                .build();

    }
}