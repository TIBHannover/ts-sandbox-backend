package eu.tib.ontologyhistory.service.network;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.Commit;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GitService<T extends Commit> {

    List<DiffAdd> getDiffAdds(URI uri, Instant datetime);

    List<DiffAdd> processCommits(List<T> commits, String user, String repo, String encodedPath, URI uri);

    void processCommitPair(T commit, T parentCommit, String user, String repo, String encodedPath, List<DiffAdd> diffAdds, URI uri);

    String getRawFileUrl(URI uri, String owner, String repo, String sha, String path);

    List<T> getCommits(URI uri, String owner, String repo, String path, String branch, Instant datetime);

    List<T> getCommits(URI uri);

    String getUserFromUrl(URI uri);

    String getRepoFromUrl(URI uri);

    String getBranchFromUrl(URI uri);

    String getEncodedPath(URI uri);

}
