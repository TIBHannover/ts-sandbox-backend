package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Mapping {

    private String name;

    private List<MappingDto> mappingDtoList;


    private List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoList;

}