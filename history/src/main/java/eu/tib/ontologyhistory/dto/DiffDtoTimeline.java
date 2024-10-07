package eu.tib.ontologyhistory.dto;

import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;

import java.util.List;

public record DiffDtoTimeline(
        DiffDto diffDto,

        List<String> graphs,

        GitDiffDto gitDiff
) {
}
