package eu.tib.ts.repository;

import eu.tib.ts.model.external.mapping.ExternalMappingData;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MappingsMongoDBRepository extends MongoRepository<ExternalMappingData, String> {
}
