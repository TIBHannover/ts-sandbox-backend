package eu.tib.ts.assessments.model.tags;

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
    Integer watchers;
    Integer forks;
    Integer stars;
    Boolean hasReleases;
    Boolean hasReadMe;
    Boolean hasLicense;
    float dataAssessmentScore;
    float communityAssessmentScore;

}
