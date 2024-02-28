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
        @JsonView({Views.Short.class})
        String id,

        @JsonView({Views.Short.class, Views.Add.class})
        String url,

        @JsonView({Views.Edit.class})
        String name,

        @JsonView({Views.Edit.class})
        String description,

        @JsonView({Views.Update.class, Views.Full.class})
        List<Diff> diffs,

        @JsonView(Views.Full.class)
        List<ApiError> invalidDiffs,

        @JsonView({Views.Short.class})
        CommitStatus commitStatus,

        @JsonView({Views.Short.class})
        String type,

        @JsonView({Views.Short.class})
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant atime
) {}
