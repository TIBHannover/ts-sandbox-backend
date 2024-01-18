package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "ontologies")
public class Ontology {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String url;

    private String name;

    private String description;

    private List<Diff> diffs;

    private List<ApiError> invalidDiffs;

    private CommitStatus commitStatus;

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant atime;
}
