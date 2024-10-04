package eu.tib.ontologyhistory.service.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.github.Commit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class GithubService implements GitService<Commit> {

    private static final String ACCESS_TOKEN = "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";

    @Override
    public List<DiffAdd> getDiffAdds(String url) {
        val uri = checkUriValidity(url);
        return getDiffAddsFrom(uri, null);
    }

    @Override
    public List<DiffAdd> getDiffAdds(String link, Instant datetime) {
        val uri = checkUriValidity(link);
        return getDiffAddsFrom(uri, datetime);
    }

    @Override
    public List<DiffAdd> getDiffAddsFrom(Optional<URI> uri, Instant datetime) {
        List<DiffAdd> diffAdds = new ArrayList<>();
        if (uri.isPresent()) {
            String user = getUserFromUrl(uri.get());
            String repo = getRepoFromUrl(uri.get());
            String encodedPath = getEncodedPath(uri.get().getPath());

            Optional<List<Commit>> commits = getCommits(uri.get(), user, repo, encodedPath, datetime);
            commits.ifPresent(commitList -> {
                Collections.reverse(commits.get());
                diffAdds.addAll(processCommits(commitList, user, repo, encodedPath, uri.get()));
            });
        }
        return diffAdds;
    }

    @Override
    public List<DiffAdd> processCommits(List<Commit> commits, String user, String repo, String encodedPath, URI uri) {
        List<DiffAdd> diffAdds = new ArrayList<>();
        ListIterator<Commit> iterator = commits.listIterator();
        Commit current = null;
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
    public void processCommitPair(Commit commit, Commit parentCommit, String user, String repo, String encodedPath, List<DiffAdd> diffAdds, URI uri) {
        Optional<String> rawFile = getRawFileUrl(uri, user, repo, commit.sha(), encodedPath);
        Optional<String> parentRawFile = getRawFileUrl(uri, user, repo, parentCommit.sha(), encodedPath);

        if (rawFile.isPresent() && parentRawFile.isPresent()) {
            DiffAdd diffAdd = new DiffAdd(
                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, repo, commit.sha(), encodedPath),
                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, repo, parentCommit.sha(), encodedPath),
                    commit.html_url(),
                    parentCommit.html_url(),
                    rawFile.get(),
                    parentRawFile.get(),
                    commit.sha(),
                    parentCommit.sha(),
                    commit.commit().committer().date(),
                    parentCommit.commit().committer().date(),
                    commit.commit().message(),
                    parentCommit.commit().message()
            );
            diffAdds.add(diffAdd);
        }
    }

    @Override
    public Optional<String> getRawFileUrl(URI uri, String owner, String repo, String sha, String path) {

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
            return Optional.of(responseRawParentFile.body());
        } catch (InterruptedException e) {
            log.error("Interrupted with the response: " + e);
            Thread.currentThread().interrupt();
        } catch (IOException e) { log.error("IOException happened: " + e); }
        return Optional.empty();
    }

    @Override
    public Optional<List<Commit>> getCommits(URI uri, String owner, String repo, String path, Instant datetime) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(uri)
                .host("api.github.com")
                .replacePath("/repos/{owner}/{repo}/commits");

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
            return Optional.of(objectMapper.readValue(response.body(), new TypeReference<>() {}));
        } catch (InterruptedException e) {
            log.error("Interrupted with the response: " + e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("IOException happened: " + e);
        }
        return Optional.of(Collections.emptyList());
    }

    @Override
    public Optional<List<Commit>> getCommits(URI uri) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String encodedPath = getEncodedPath(uri.getPath());
        return getCommits(uri, user, repo, encodedPath, null);
    }

    @Override
    public Optional<URI> checkUriValidity(String url) {
        try {
            val uri = new URI(url);
            return Optional.of(uri);
        } catch (URISyntaxException uriSyntaxException) {
            log.error("URISyntaxException with the provided URL: " + url);
        }
        return Optional.empty();
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
        return uri.getPath().split("/")[5];
    }

    @Override
    public String getEncodedPath(String url) {
        String[] segments = url.split("/");
        int startIndex = url.contains("/refs/heads") ? 6 : 4;
        return String.join("/", Arrays.copyOfRange(segments, startIndex, segments.length));
    }

}
