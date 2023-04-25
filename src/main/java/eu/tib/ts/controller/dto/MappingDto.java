package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import lombok.Builder;
import lombok.Value;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.Collection;

@Builder
@Value
public class MappingDto {

    long id;

    String mappingId;

    String sourceIRI;
    String targetIRI;

    int typeOfMapping;

    public static MappingDto getMappingsDto(ProcessedMapping processedMapping) {

        return MappingDto.builder()
                .id(processedMapping.getId())
                .mappingId(processedMapping.getMappingId())
                .sourceIRI(processedMapping.getSourceIRI())
                .targetIRI(processedMapping.getTargetIRI())
                .typeOfMapping(processedMapping.getTypeOfMapping())
                .build();

    }
}