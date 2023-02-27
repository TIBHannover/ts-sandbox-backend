package eu.tib.ts.controller.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class MappingDto {

    long id;
    String mappingId;
    String mappingUrl;
    String mappingTitle;




}
