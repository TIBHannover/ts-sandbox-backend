package eu.tib.ontologyhistory.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
@Jacksonized
public class InvalidContoDiff {

    @Id
    private String id;

    private String sha;

    private String parentSha;

    private String message;
}
