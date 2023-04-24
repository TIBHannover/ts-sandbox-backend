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
    String ontologyId;
    String mappingId;
    OWLOntology sourceOntology;
    OWLOntology targetOntology;
    Collection<MappingObjectStr> mappingObjectStrs;

    public static MappingDto getMappingDto(ProcessedMapping processedMapping) {

        return MappingDto.builder()
                .id(processedMapping.getId())
                .mappingId(processedMapping.getMappingId())
                .sourceOntology(processedMapping.getSourceOntology())
                .targetOntology(processedMapping.getTargetOntology())
                .mappingObjectStrs(processedMapping.getMappingObjectStrs())
                .build();

    }
}