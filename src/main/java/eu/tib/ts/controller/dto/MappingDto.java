package eu.tib.ts.controller.dto;
import eu.tib.ts.model.ontology.ProcessedMapping;
import lombok.Builder;
import lombok.Value;


import java.util.Collection;
import java.util.Set;

/**
 * All information about mappings between a pair of ontologies stored in MongoDB database
 */
@Builder
@Value
public class MappingDto {

    long id;

    String mappingId;

    Set<OntologyDto> sourceOntology;

    Set<OntologyDto> targetOntology;

    int numberOfMappings;

    int numberOfConflictiveMappings;

    Set<MappingObjectSetModel> mappingList;

    Set<MappingObjectSetModel> conflictiveMappingsList;

    public static MappingDto getMappingObjectStrDto(ProcessedMapping processedMapping) {

        return MappingDto.builder()
                .id(processedMapping.getId())
                .mappingId(processedMapping.getMappingId())
                .numberOfMappings(processedMapping.getNumberOfMappings())
                .numberOfConflictiveMappings(processedMapping.getNumberOfConflictiveMappings())
                .sourceOntology(processedMapping.getSourceOntology())
                .targetOntology(processedMapping.getTargetOntology())
                .mappingList(processedMapping.getMappingList())
                .conflictiveMappingsList(processedMapping.getConflictiveMappingsList())
                .build();

    }

}