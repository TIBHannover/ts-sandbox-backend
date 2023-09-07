package eu.tib.ontologyhistory.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Getter
@Setter
@Document(collection = "history")
public class History {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String treeId;

    private String name;

    private List<String> parent;

    private List<String> des;

    private String commitSha1;

    private String attributes;

}
