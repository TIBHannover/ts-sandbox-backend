package eu.tib.ts.repository;

import eu.tib.ts.model.ontology.ProcessedOntology;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedOntologyRepository extends CrudRepository<ProcessedOntology, Long> {
}
