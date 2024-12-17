package eu.tib.ts.assessments.model.tags;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "community_assessment_details")
public class CommunityAssessmentDetails {
    String ontologyId;
    String title;
    String repoUrl;
    Integer watches;
    Integer forks;
    Integer likes;
    float communityAssessmentScore;
}
