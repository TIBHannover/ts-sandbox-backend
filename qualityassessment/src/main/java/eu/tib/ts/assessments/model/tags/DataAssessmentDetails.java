package eu.tib.ts.assessments.model.tags;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "data_assessment_details")
public class DataAssessmentDetails {
    String ontologyId;
    String title;
    String repoUrl;
    Boolean releases;
    Boolean readMe;
    Boolean license;
    float dataAssessmentScore;
}
