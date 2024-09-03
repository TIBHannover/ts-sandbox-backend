package eu.tib.ontologyhistory.dto.diff;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.model.Axiom;
import eu.tib.ontologyhistory.view.Views;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.bson.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiffDto (

        @JsonView({Views.Short.class})
        String id,

        @JsonView({Views.Short.class})
        String url,

        @JsonView({Views.Edit.class})
        String sha,

        @JsonView({Views.Edit.class})
        String parentSha,

        @JsonView({Views.Edit.class})
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant datetime,

        @JsonView({Views.Edit.class})
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant parentDatetime,

        @JsonView({Views.Full.class})
        @Schema(description = "Markdown diff content", implementation = java.lang.String.class)
        Document markdown,

        @JsonView({Views.Add.class})
        @ArraySchema(schema = @Schema(description = "List of axioms", implementation = Axiom.class), minItems = 0)
        Map<String, List<Axiom>> axioms,

        @JsonView({Views.Full.class})
        String gitDiff,

        @JsonView({Views.Edit.class})
        String message
) {}
