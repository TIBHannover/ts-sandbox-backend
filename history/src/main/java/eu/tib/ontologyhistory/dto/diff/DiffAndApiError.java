package eu.tib.ontologyhistory.dto.diff;

import eu.tib.ontologyhistory.model.ApiError;

import java.util.List;

public record DiffAndApiError (
        List<DiffDto> diffs,

        List<ApiError> apiErrors
) { }
