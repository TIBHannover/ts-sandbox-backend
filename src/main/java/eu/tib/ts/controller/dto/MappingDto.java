package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.MappingCharacteristicsInfo;
import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import lombok.Builder;
import lombok.Value;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

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