package eu.tib.ontologyhistory.repository;

import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.GitDiff;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitDiffRepository extends MongoRepository<GitDiff, String> {

    GitDiff findFirstBySha(String sha);

    GitDiff findFirstByUrl(String url);

    void deleteAllByUrl(String url);
}
