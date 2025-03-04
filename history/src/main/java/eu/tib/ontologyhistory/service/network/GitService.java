package eu.tib.ontologyhistory.service.network;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitServiceRequest;
import eu.tib.ontologyhistory.model.Commit;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public interface GitService<T extends Commit> {

    List<DiffAdd> getDiffAdds(URI uri, Instant datetime);

    List<DiffAdd> processCommits(URI uri, List<T> commits, GitServiceRequest request);

    void processCommitPair(URI uri, T commit, T parentCommit, GitServiceRequest request, List<DiffAdd> diffAdds);

    String getRawFileUrl(URI uri, GitServiceRequest request, String sha);

    List<T> getCommits(URI uri, GitServiceRequest request, Instant datetime);

    List<T> getCommits(URI uri);

    String getOwnerFromUrl(URI uri);

    String getRepoFromUrl(URI uri);

    String getBranchFromUrl(URI uri);

    String getEncodedPath(URI uri);

}
