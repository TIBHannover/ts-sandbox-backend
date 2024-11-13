package eu.tib.ontologyhistory.service.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.github.GithubCommit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class GithubService implements GitService<GithubCommit> {

    private static final String GITHUB_SECRET_FILE = "github_access_token.txt.secret";

    private static final String ACCESS_TOKEN;

    static {
        try {
            ACCESS_TOKEN = Files.readString(Path.of("github_access_token.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DiffAdd> getDiffAdds(URI uri, Instant datetime) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String branch = getBranchFromUrl(uri);
        String encodedPath = getEncodedPath(uri);

        List<GithubCommit> commits = getCommits(uri, user, repo, encodedPath, branch, datetime);
        Collections.reverse(commits);
        return new ArrayList<>(processCommits(commits, user, repo, encodedPath, uri));
    }

    @Override
    public List<DiffAdd> processCommits(List<GithubCommit> githubCommits, String user, String repo, String encodedPath, URI uri) {
        List<DiffAdd> diffAdds = new ArrayList<>();
        ListIterator<GithubCommit> iterator = githubCommits.listIterator();
        GithubCommit current = null;
        while (iterator.hasNext()) {
            val next = iterator.next();
            if (current != null) {
                processCommitPair(current, next, user, repo, encodedPath, diffAdds, uri);
            }
            current = next;
        }
        return diffAdds;
    }

    @Override
    public void processCommitPair(GithubCommit githubCommit, GithubCommit parentGithubCommit, String user, String repo, String encodedPath, List<DiffAdd> diffAdds, URI uri) {
        String rawFile = getRawFileUrl(uri, user, repo, githubCommit.sha(), encodedPath);
        String parentRawFile = getRawFileUrl(uri, user, repo, parentGithubCommit.sha(), encodedPath);

        if (rawFile != null && parentRawFile != null) {
            DiffAdd diffAdd = new DiffAdd(
                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, repo, githubCommit.sha(), encodedPath),
                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, repo, parentGithubCommit.sha(), encodedPath),
                    githubCommit.html_url(),
                    parentGithubCommit.html_url(),
                    rawFile,
                    parentRawFile,
                    githubCommit.sha(),
                    parentGithubCommit.sha(),
                    githubCommit.commit().committer().date(),
                    parentGithubCommit.commit().committer().date(),
                    githubCommit.commit().message(),
                    parentGithubCommit.commit().message()
            );
            diffAdds.add(diffAdd);
        }
    }

    @Override
    public String getRawFileUrl(URI uri, String owner, String repo, String sha, String path) {

        URI githubRawFileApi = UriComponentsBuilder.fromUri(uri)
                .host("raw.githubusercontent.com")
                .replacePath("/{owner}/{repo}/{sha}/{path}")
                .buildAndExpand(owner, repo, sha, path)
                .toUri();

        HttpRequest requestGetRawFile = HttpRequest.newBuilder()
                .uri(githubRawFileApi)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> responseRawParentFile = client.send(requestGetRawFile, HttpResponse.BodyHandlers.ofString());
            return responseRawParentFile.body();
        } catch (InterruptedException e) {
            log.error("Interrupted with the response: " + e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("IOException happened: " + e);
        }
        return null;
    }

    @Override
    public List<GithubCommit> getCommits(URI uri, String owner, String repo, String path, String branch, Instant datetime) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(uri)
                .host("api.github.com")
                .replacePath("/repos/{owner}/{repo}/commits");

        uriBuilder.queryParam("sha", branch);

        if (path != null) {
            uriBuilder.queryParam("path", path);
        }

        if (datetime != null) {
            uriBuilder.queryParam("since", datetime);
        }

        URI githubApiUri = uriBuilder
                .buildAndExpand(owner, repo)
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(githubApiUri)
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            return objectMapper.readValue(response.body(), new TypeReference<>() {
            });
        } catch (InterruptedException e) {
            log.error("Interrupted with the response: " + e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("IOException happened: " + e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<GithubCommit> getCommits(URI uri) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String branch = getBranchFromUrl(uri);
        String encodedPath = getEncodedPath(uri);
        return getCommits(uri, user, repo, encodedPath, branch, null);
    }

    @Override
    public String getUserFromUrl(URI uri) {
        return uri.getPath().split("/")[1];
    }

    @Override
    public String getRepoFromUrl(URI uri) {
        return uri.getPath().split("/")[2];
    }

    @Override
    public String getBranchFromUrl(URI uri) {
        int startIndex = (uri.toString().contains("/refs/heads") || uri.toString().contains("/refs/tags")) ? 5 : 3;
        return uri.getPath().split("/")[startIndex];
    }

    @Override
    public String getEncodedPath(URI uri) {
        val path = uri.getPath();
        String[] segments = path.split("/");
        int startIndex = (path.contains("/refs/heads") || path.contains("/refs/tags")) ? 6 : 4;
        return String.join("/", Arrays.copyOfRange(segments, startIndex, segments.length));
    }

}
