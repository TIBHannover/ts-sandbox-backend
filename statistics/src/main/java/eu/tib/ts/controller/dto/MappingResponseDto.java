package eu.tib.ts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Response DTO for mapping data matching the new schema
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class MappingResponseDto {
    
    private String id;
    
    private String mappingId;
    
    private OntologyDto sourceOntology;
    
    private StatisticsDto statistics;
    
    private Set<TargetOntologyObjectSetModel> targetOntologyList;
    
    private MappingMetadataDto metadata;

    public MappingResponseDto() {
    }
}
