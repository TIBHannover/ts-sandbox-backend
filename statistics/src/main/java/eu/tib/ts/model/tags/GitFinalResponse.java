package eu.tib.ts.model.tags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitFinalResponse {

    String ontologyId;
    String title;
    String repoUrl;
    Integer watches;
    Integer forks;
    Integer likes;
    Boolean releases;
    Boolean readMe;
    Boolean license;
    float booleanEstimation;


}
