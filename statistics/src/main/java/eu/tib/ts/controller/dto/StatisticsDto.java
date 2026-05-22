package eu.tib.ts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Statistics about mappings and target ontologies
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class StatisticsDto {
    private int numberOfTargetOntologies;
    private int numberOfMappings;
    private int numberOfConflictiveMappings;

    public StatisticsDto() {
    }
}
