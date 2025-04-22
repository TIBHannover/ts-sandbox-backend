package eu.tib.ontologyhistory.model.gitlab;

import com.fasterxml.jackson.annotation.*;
import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.view.Views;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitlabCommit(

        @JsonView(Views.Short.class)
        @JsonProperty("sha")
        @JsonAlias("id")
        String id,

        String web_url,

        @JsonView(Views.Short.class)
        String message,

        @JsonView(Views.Short.class)
        @JsonProperty("date")
        @JsonAlias("committed_date")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", timezone = "UTC")
        Instant committed_date,

        List<String> parent_ids
) implements Commit {

    @Override
    public Instant getDatetime() {
        return committed_date;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ParentId(
            String parentId
    ) {}
}
