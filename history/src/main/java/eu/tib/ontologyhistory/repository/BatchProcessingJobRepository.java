package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.BatchProcessingJob;
import eu.tib.ontologyhistory.model.BatchJobStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchProcessingJobRepository extends MongoRepository<BatchProcessingJob, String> {
    List<BatchProcessingJob> findAllByStatus(BatchJobStatus status);
}
