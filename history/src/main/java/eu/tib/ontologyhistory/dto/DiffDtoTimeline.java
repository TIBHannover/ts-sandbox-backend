package eu.tib.ontologyhistory.dto;

import eu.tib.ontologyhistory.dto.conto.GraphInfo;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.model.GitDiff;

import java.util.List;

public record DiffDtoTimeline(
        DiffDto diffDto,

        List<GraphInfo> graphs,

        GitDiff gitDiff
) {
}
