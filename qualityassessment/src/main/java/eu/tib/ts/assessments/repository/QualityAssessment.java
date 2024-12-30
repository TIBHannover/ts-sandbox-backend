package eu.tib.ts.assessments.repository;

import eu.tib.ts.assessments.model.tags.QualityAssessmentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QualityAssessment extends MongoRepository<QualityAssessmentDetails,String> {

}