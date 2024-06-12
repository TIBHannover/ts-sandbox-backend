package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.Diff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends MongoRepository<Diff, String> {

    List<Diff> findAllByUrl(String url);

    Optional<Diff> findBySha(String sha);

    void deleteAllByUrl(String url);
}
