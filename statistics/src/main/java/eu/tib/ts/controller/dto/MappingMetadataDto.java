package eu.tib.ts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Metadata information about mappings
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class MappingMetadataDto {
    private String matchingTool;
    private String computedAt;
    private String version;

    public MappingMetadataDto() {
    }
}
