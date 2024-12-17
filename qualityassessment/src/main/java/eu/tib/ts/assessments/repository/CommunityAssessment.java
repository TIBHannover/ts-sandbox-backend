package eu.tib.ts.assessments.repository;

import eu.tib.ts.assessments.model.tags.CommunityAssessmentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommunityAssessment extends MongoRepository<CommunityAssessmentDetails,String> {

}