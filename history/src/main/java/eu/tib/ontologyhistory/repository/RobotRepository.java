package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.Diff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RobotRepository extends MongoRepository<Diff, String> {

    List<Diff> findAllByUri(URI uri);

    @Query(value = "{}", fields = "{ 'uri' :  1 }")
    Set<URI> findAllUris();

    Diff findFirstByUri(URI uri);

    Optional<Diff> findFirstBySha(String sha);

    Optional<Diff> findFirstByParentSha(String parentSha);

    void deleteAllByUri(URI uri);
}
