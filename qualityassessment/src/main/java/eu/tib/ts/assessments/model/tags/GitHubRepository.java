package eu.tib.ts.assessments.model.tags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubRepository {
    @JsonProperty("stargazers_count")
    private int stargazersCount;

    @JsonProperty("subscribers_count")
    private int subscribersCount;

    @JsonProperty("forks_count")
    private int forksCount;

    // Add other fields if needed
}

