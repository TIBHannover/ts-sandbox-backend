package eu.tib.ts.controller.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
@Value
public class SharedPropertyUriDto {
    Map<String, List<OntologyDto>> sharedPropertyUri;

    public static SharedPropertyUriDto empty() {
        return SharedPropertyUriDto.builder()
            .sharedPropertyUri(Collections.emptyMap())
            .build();
    }
}
