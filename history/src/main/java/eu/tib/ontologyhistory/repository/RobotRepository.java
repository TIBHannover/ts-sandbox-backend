package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.Diff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends MongoRepository<Diff, String> {

    List<Diff> findAllByUrl(URI uri);

    Diff findFirstByUrl(URI uri);

    Optional<Diff> findFirstBySha(String sha);

    Optional<Diff> findFirstByParentSha(String parentSha);

    void deleteAllByUrl(URI uri);
}
