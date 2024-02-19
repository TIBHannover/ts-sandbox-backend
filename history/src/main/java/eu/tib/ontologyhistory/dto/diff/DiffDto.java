package eu.tib.ontologyhistory.dto.diff;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import eu.tib.ontologyhistory.model.Axiom;
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

        String id,

        String ontologyId,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant timestamp,

        String sha,

        String parentSha,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant shaOffsetDateTime,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant parentOffsetDateTime,

        @Schema(description = "Markdown diff content", implementation = java.lang.String.class)
        Document markdown,

        @ArraySchema(schema = @Schema(description = "List of axioms", implementation = Axiom.class), minItems = 0)
        Map<String, List<Axiom>> axioms,

        String message
) {}
