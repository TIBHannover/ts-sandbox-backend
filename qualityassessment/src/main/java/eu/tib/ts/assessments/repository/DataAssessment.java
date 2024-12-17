package eu.tib.ts.assessments.repository;

import eu.tib.ts.assessments.model.tags.DataAssessmentDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataAssessment extends MongoRepository<DataAssessmentDetails,String> {

}