package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.GitDiff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;

@Repository
public interface GitDiffRepository extends MongoRepository<GitDiff, String> {

    GitDiff findFirstBySha(String sha);

    GitDiff findFirstByParentSha(String parentSha);

    GitDiff findFirstByUri(URI uri);

    List<GitDiff> findAllByUri(URI uri);

    GitDiff findFirstByUriOrderByDatetimeDesc(URI uri);

    void deleteAllByUri(URI uri);
}
