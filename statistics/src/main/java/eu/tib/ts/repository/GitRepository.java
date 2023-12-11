package eu.tib.ts.repository;

import eu.tib.ts.model.tags.GitRepo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitRepository extends MongoRepository<GitRepo,String> {

}
