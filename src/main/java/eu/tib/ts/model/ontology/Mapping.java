package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingDto;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Mapping {

    String mappingName ;
    List<MappingDto> mappingDtoList;
}
