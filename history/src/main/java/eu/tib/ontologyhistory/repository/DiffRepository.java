package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.Diff;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DiffRepository extends MongoRepository<Diff, String> {
    Diff findBySha(String sha);

    List<Diff> findAllByOntologyId(String ontologyId);
}
