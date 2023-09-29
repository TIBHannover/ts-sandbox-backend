package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Mapping {

    private String name;

    private List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoList;

}