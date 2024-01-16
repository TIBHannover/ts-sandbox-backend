package eu.tib.ontologyhistory.dto.ontology;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.model.ApiError;
import eu.tib.ontologyhistory.model.CommitStatus;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.view.Views;

import java.time.Instant;
import java.util.List;

public record OntologyDto(
        @JsonView(Views.Swagger.class)
        String id,

        @JsonView(Views.Swagger.class)
        String url,

        @JsonView(Views.Edit.class)
        String name,

        @JsonView(Views.Edit.class)
        String description,

        @JsonView(Views.Update.class)
        List<Diff> diffs,

        @JsonView(Views.Swagger.class)
        List<ApiError> invalidDiffs,

        @JsonView(Views.Swagger.class)
        CommitStatus commitStatus,

        @JsonView(Views.Swagger.class)
        String type,

        @JsonView(Views.Swagger.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant atime
) {}
