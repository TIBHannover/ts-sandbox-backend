package eu.tib.ts.controller.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
@Value
public class SharedClassUriDto {
    Map<String, List<OntologyDto>> sharedClassUri;

    public static SharedClassUriDto empty() {
        return SharedClassUriDto.builder()
            .sharedClassUri(Collections.emptyMap())
            .build();
    }
}
