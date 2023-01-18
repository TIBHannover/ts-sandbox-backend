package eu.tib.ts.repository;

import eu.tib.ts.model.ontology.ProcessedOntology;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigInteger;
import java.util.List;

public interface ProcessedMongoOntologyRepository extends MongoRepository<ProcessedOntology, Integer> {
    List<ProcessedOntology> findByOntologyIdIn(List<String> ids);
}
