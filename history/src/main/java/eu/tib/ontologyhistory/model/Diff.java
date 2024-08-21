package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;
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
    @JsonView({Views.Full.class})
    private String id;

    @JsonView({Views.Full.class})
    private String url;

    @JsonView({Views.Full.class})
    private String sha;

    @JsonView({Views.Full.class})
    private String parentSha;

    @JsonView({Views.Full.class})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant datetime;

    @JsonView({Views.Full.class})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant parentDatetime;

    @JsonView({Views.Full.class})
    @Schema(description = "Markdown diff content", implementation = java.lang.String.class)
    private org.bson.Document markdown;

    @JsonView({Views.Full.class})
    @ArraySchema(schema = @Schema(description = "List of axioms", implementation = Axiom.class), minItems = 0)
    private Map<String, List<Axiom>> axioms;

    @JsonView({Views.Full.class})
    private String message;

}
