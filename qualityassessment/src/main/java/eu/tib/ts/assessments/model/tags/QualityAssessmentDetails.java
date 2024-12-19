package eu.tib.ts.assessments.model.tags;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "quality_assessment_details")
public class QualityAssessmentDetails {
    String ontologyId;
    String title;
    String repoUrl;
    Boolean hasReleases;
    Boolean hasReadMe;
    Boolean hasLicense;
    Integer watchers;
    Integer forks;
    Integer stars;
    float dataAssessmentScore;
    float communityAssessmentScore;
}
