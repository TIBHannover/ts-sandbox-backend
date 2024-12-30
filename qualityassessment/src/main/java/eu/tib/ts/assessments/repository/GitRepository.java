package eu.tib.ts.assessments.repository;



import eu.tib.ts.assessments.model.tags.GitRepo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GitRepository extends MongoRepository<GitRepo,String> {

}