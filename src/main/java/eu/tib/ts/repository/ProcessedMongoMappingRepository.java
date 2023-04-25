package eu.tib.ts.repository;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProcessedMongoMappingRepository extends MongoRepository<ProcessedMapping, Integer> {

    List<ProcessedMapping> findByMappingIdIn(List<String> ids);
}
