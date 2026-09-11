package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.InvalidContoDiff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidContoDiffRepository extends MongoRepository<InvalidContoDiff, String>  {

    InvalidContoDiff findFirstByParentShaOrderByCreatedAtDesc(String parentSha);

    void deleteAllByUri(String uri);

    void deleteAllByUriAndParentSha(String uri, String parentSha);
}
