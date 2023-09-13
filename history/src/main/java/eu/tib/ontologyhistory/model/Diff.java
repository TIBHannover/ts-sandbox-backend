package eu.tib.ontologyhistory.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "diff")
public class Diff {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String ontologyId;

    private Instant timestamp;

    private String parentSha;

    private List<String> children;

    private String sha;

    private List<String> value;

    private String message;

    public Diff(String s) {
    }
}
