package eu.tib.ts.repository;

import eu.tib.ts.model.tags.GitFinalResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitMediaCollection extends MongoRepository<GitFinalResponse,String> {

}

