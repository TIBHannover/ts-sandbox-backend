package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@Jacksonized
public class Diff {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String ontologyId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    private String sha;

    private String parentSha;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant shaOffsetDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant parentOffsetDateTime;

    @Schema(description = "Markdown diff content", implementation = java.lang.String.class)
    private org.bson.Document markdown;

    @ArraySchema(schema = @Schema(description = "List of axioms", implementation = Axiom.class), minItems = 0)
    private Map<String, List<Axiom>> axioms;

    private String message;

}
