package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.GitDiff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GitDiffRepository extends MongoRepository<GitDiff, String> {

    GitDiff findFirstBySha(String sha);

    GitDiff findFirstByParentSha(String parentSha);

    GitDiff findFirstByUrl(String url);

    List<GitDiff> findAllByUrl(String url);

    GitDiff findFirstByUrlOrderByDatetimeDesc(String url);

    void deleteAllByUrl(String url);
}
