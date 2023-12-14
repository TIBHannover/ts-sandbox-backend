package eu.tib.ts.model.tags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "git_final_responses")
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
    float valuesEstimation;


}
