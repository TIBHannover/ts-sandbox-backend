package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.GitDiff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Repository
public interface GitDiffRepository extends MongoRepository<GitDiff, String> {

    GitDiff findFirstBySha(String sha);

    GitDiff findFirstByParentSha(String parentSha);

    @Query(value = "{}", fields = "{ 'uri' :  1 }")
    Set<URI> findAllUris();

    GitDiff findFirstByUri(URI uri);

    List<GitDiff> findAllByUri(URI uri);

    GitDiff findFirstByUriOrderByDatetimeDesc(URI uri);

    void deleteAllByUri(URI uri);
}
