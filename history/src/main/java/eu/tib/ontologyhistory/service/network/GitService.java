package eu.tib.ontologyhistory.service.network;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GitService<T> {

    List<DiffAdd> getDiffAdds(String url);

    List<DiffAdd> getDiffAdds(String link, Instant datetime);

    List<DiffAdd> getDiffAddsFrom(Optional<URI> uri, Instant datetime);

    List<DiffAdd> processCommits(List<T> commits, String user, String repo, String encodedPath, URI uri);

    void processCommitPair(T commit, T parentCommit, String user, String repo, String encodedPath, List<DiffAdd> diffAdds, URI uri);

    Optional<String> getRawFileUrl(URI uri, String owner, String repo, String sha, String path);

    Optional<List<T>> getCommits(URI uri, String owner, String repo, String path, String branch, Instant datetime);

    Optional<List<T>> getCommits(URI uri);

    Optional<List<T>> getCommits(URI uri, Instant datetime);

    Optional<URI> checkUriValidity(String url);

    String getUserFromUrl(URI uri);

    String getRepoFromUrl(URI uri);

    String getBranchFromUrl(URI uri);

    String getEncodedPath(String url);
}
