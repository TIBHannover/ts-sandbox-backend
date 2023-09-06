package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedMapping;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

/**
 * Stores all mappings grouped by source ontologies. Additionally, it contains
 * number of target ontologies. That number excludes in count those target ontologies for which
 * number of mappings and number of conflictive mappings is equal to zero.
 */
@Builder
@Value
public class MappingGropedBySourceOntologyDto {

    long id;

    String mappingId;

    Set<OntologyDto> sourceOntology;

    int numberOfTargetOntologies;

    Set<TargetOntologyListObjectSetModel> targetOntologyList;

    public static MappingGropedBySourceOntologyDto getMappingGroupedBySourceOntologyObjectStrDto(ProcessedMapping processedMapping) {

        return MappingGropedBySourceOntologyDto.builder()
                .id(processedMapping.getId())
                .mappingId(processedMapping.getMappingId())
                .sourceOntology(processedMapping.getSourceOntology())
                .numberOfTargetOntologies(processedMapping.getNumberOfTargetOntologies())
                .targetOntologyList(processedMapping.getTargetOntologyList())
                .build();
    }
}