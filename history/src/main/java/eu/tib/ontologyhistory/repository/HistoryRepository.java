package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.History;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoryRepository extends MongoRepository<History, String> {

    List<History> findByTreeId(String id);

}
