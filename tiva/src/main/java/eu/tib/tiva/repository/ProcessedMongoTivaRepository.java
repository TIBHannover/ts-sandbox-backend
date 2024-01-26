package eu.tib.tiva.repository;

import eu.tib.tiva.model.ProcessedTiva;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProcessedMongoTivaRepository  extends MongoRepository<ProcessedTiva, Integer> {

    List<ProcessedTiva> findByTivaIdIn(List<String> ids);

}
