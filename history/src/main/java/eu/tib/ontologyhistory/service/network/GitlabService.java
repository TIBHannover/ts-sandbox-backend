package eu.tib.ontologyhistory.service.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.gitlab.GitlabCommit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@AllArgsConstructor
public class GitlabService implements GitService<GitlabCommit> {

    private static final String ACCESS_TOKEN = "glpat-TAU5FEyCBcyBbwDsMb5J";

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public List<DiffAdd> getDiffAdds(URI uri, Instant datetime) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String branch = getBranchFromUrl(uri);
        String encodedPath = getEncodedPath(uri);

        List<GitlabCommit> commits = getCommits(uri, user, repo, branch, encodedPath, datetime);
        commits.sort(Comparator.comparing(GitlabCommit::committed_date));
        return new ArrayList<>(processCommits(commits, user, repo, encodedPath, uri));
    }

    @Override
    public List<DiffAdd> processCommits(List<GitlabCommit> commits, String user, String repo, String encodedPath, URI uri) {
        List<DiffAdd> diffAdds = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ListIterator<GitlabCommit> iterator = commits.listIterator();
        GitlabCommit current = null;
        while (iterator.hasNext()) {
            val next = iterator.next();
            if (current != null) {
                GitlabCommit finalCurrent = current;
                futures.add(CompletableFuture.runAsync(() -> processCommitPair(finalCurrent, next, user, repo, encodedPath, diffAdds, uri), executor));
            }
            current = next;
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return diffAdds;
    }

    @Override
    public void processCommitPair(GitlabCommit commit, GitlabCommit parentCommit, String user, String repo, String encodedPath, List<DiffAdd> diffAdds, URI uri) {
        String rawFile = getRawFileUrl(uri, user, repo, commit.id(), encodedPath);
        String parentRawFile = getRawFileUrl(uri, user, repo, parentCommit.id(), encodedPath);

        if (rawFile != null && parentRawFile != null) {
            DiffAdd diffAdd = new DiffAdd(
                    String.format("https://gitlab.com/%s/%s/-/raw/%s/%s", user, repo, commit.id(), encodedPath),
                    String.format("https://gitlab.com/%s/%s/-/raw/%s/%s", user, repo, parentCommit.id(), encodedPath),
                    commit.web_url(),
                    parentCommit.web_url(),
                    rawFile,
                    parentRawFile,
                    commit.id(),
                    parentCommit.id(),
                    commit.committed_date(),
                    parentCommit.committed_date(),
                    commit.message(),
                    parentCommit.message()
            );
            synchronized (this) {
                diffAdds.add(diffAdd);
            }
        }
    }

    @Override
    public String getRawFileUrl(URI uri, String owner, String repo, String sha, String path) {

        String link = "https://gitlab.com/api/v4/projects/" + owner + "%2F" + repo + "/repository/files/" + UriUtils.encode(path, StandardCharsets.UTF_8) + "/raw?ref=" + sha;

        URI gitlabUri = URI.create(link);

        HttpRequest requestGetRawFile = HttpRequest.newBuilder()
                .uri(gitlabUri)
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
    public List<GitlabCommit> getCommits(URI uri, String owner, String repo, String path, String ref, Instant datetime) {

        String link = "https://gitlab.com/api/v4/projects/" + owner + "%2F" + repo + "/repository/commits";

        if (path != null) {
            link += "?path=" + path;
        }

        if (datetime != null) {
            link += "&since=" + datetime;
        }


        List<GitlabCommit> result = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();


        URI gitlabUri = URI.create(link);
        Optional<String> nextPage = Optional.of("1");

        while (nextPage.isPresent()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(gitlabUri)
                    .header("Authorization", "Bearer " + ACCESS_TOKEN)
                    .build();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                List<GitlabCommit> commits = objectMapper.readValue(response.body(), new TypeReference<>() {
                });
                result.addAll(commits);

                nextPage = response.headers().firstValue("x-next-page");
                if (nextPage.isPresent() && !nextPage.get().isEmpty()) {
                    gitlabUri = URI.create(link + "&page=" + nextPage.get());
                } else {
                    nextPage = Optional.empty();
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted: {}", String.valueOf(e));
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("IoException either with sent/received information with request or on objectMapper.readValue during json serializing: {}", String.valueOf(e));
            }

        }


        return result;
    }

    @Override
    public List<GitlabCommit> getCommits(URI uri) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String branch = getBranchFromUrl(uri);
        String encodedPath = getEncodedPath(uri);
        return getCommits(uri, user, repo, branch, encodedPath, null);
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
    public String getEncodedPath(URI uri) {
        String[] segments = uri.toString().split("/");
        return String.join("/", Arrays.copyOfRange(segments, 6, segments.length));
    }
}
