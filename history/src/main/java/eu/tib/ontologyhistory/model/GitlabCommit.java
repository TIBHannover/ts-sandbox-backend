package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitlabCommit(
        String id,
        String web_url,
        String message,
        Instant committed_date,
        List<String> parent_ids
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ParentId(
            String parentId
    ) {}
}
