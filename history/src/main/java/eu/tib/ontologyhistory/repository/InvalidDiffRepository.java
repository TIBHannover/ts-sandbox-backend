package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.ApiError;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvalidDiffRepository extends MongoRepository<ApiError, String> {
}
