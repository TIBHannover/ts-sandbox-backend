package eu.tib.ontologyhistory.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "ontologies")
public class Ontology {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String name;

    private String description;

    private List<Diff> diffs;

    private List<ApiError> invalidDiffs;

    private CommitStatus commitStatus;

    private String type;
}
