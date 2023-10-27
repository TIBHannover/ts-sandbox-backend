package eu.tib.ontologyhistory.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@Jacksonized
@Document(collection = "diff")
public class Diff {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String ontologyId;

    private Instant timestamp;

    private String sha;

    private String parentSha;

    private Instant shaOffsetDateTime;

    private Instant parentOffsetDateTime;

    private org.bson.Document markdown;

    private Map<String, List<Axiom>> axioms;

    private String message;

}
