package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.Diff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiffRepository extends MongoRepository<Diff, String> {
    Diff findBySha(String sha);

    List<Diff> findAllByOntologyId(String ontologyId);
}
