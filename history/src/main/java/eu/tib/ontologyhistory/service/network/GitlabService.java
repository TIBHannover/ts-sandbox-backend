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
import java.net.URISyntaxException;
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

            Optional<List<GitlabCommit>> commits = getCommits(uri.get(), user, repo, encodedPath, datetime);
            if (commits.isPresent()) {
                commits.get().sort(Comparator.comparing(GitlabCommit::committed_date));
                diffAdds.addAll(processCommits(commits.get(), user, repo, encodedPath, uri.get()));
            }
        }
        return diffAdds;
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
                futures.add(CompletableFuture.runAsync(() -> {
                    processCommitPair(finalCurrent, next, user, repo, encodedPath, diffAdds, uri);
                }, executor));
            }
            current = next;
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return diffAdds;
    }

    @Override
    public void processCommitPair(GitlabCommit commit, GitlabCommit parentCommit, String user, String repo, String encodedPath, List<DiffAdd> diffAdds, URI uri) {
        Optional<String> rawFile = getRawFileUrl(uri, user, repo, commit.id(), encodedPath);
        Optional<String> parentRawFile = getRawFileUrl(uri, user, repo, parentCommit.id(), encodedPath);

        if (rawFile.isPresent() && parentRawFile.isPresent()) {
            DiffAdd diffAdd = new DiffAdd(
                    String.format("https://gitlab.com/%s/%s/-/raw/%s/%s", user, repo, commit.id(), encodedPath),
                    String.format("https://gitlab.com/%s/%s/-/raw/%s/%s", user, repo, parentCommit.id(), encodedPath),
                    commit.web_url(),
                    parentCommit.web_url(),
                    rawFile.get(),
                    parentRawFile.get(),
                    commit.id(),
                    parentCommit.id(),
                    commit.committed_date(),
                    parentCommit.committed_date(),
                    commit.message(),
                    parentCommit.message()
            );
            synchronized (diffAdds) {
                diffAdds.add(diffAdd);
            }
        }
    }

    @Override
    public Optional<String> getRawFileUrl(URI uri, String owner, String repo, String sha, String path) {

        String link = "https://gitlab.com/api/v4/projects/" + owner + "%2F" + repo + "/repository/files/" + UriUtils.encode(path, StandardCharsets.UTF_8) + "/raw?ref=" + sha;

        URI gitlabUri = URI.create(link);

        HttpRequest requestGetRawFile = HttpRequest.newBuilder()
                .uri(gitlabUri)
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
    public Optional<List<GitlabCommit>> getCommits(URI uri, String owner, String repo, String path, Instant datetime) {

        String link = "https://gitlab.com/api/v4/projects/" + owner + "%2F" + repo + "/repository/commits";

        if (path != null) {
            link += "?path=" + path;
        }

        if (datetime != null) {
            link += "&since=" + datetime;
        }


        List<GitlabCommit> result = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();

        try {
            URI gitlabUri = URI.create(link);
            Optional<String> nextPage = Optional.of("1");

            while (nextPage.isPresent()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(gitlabUri)
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                List<GitlabCommit> commits = objectMapper.readValue(response.body(), new TypeReference<>() {});
                result.addAll(commits);

                nextPage = response.headers().firstValue("x-next-page");
                if (nextPage.isPresent() && !nextPage.get().isEmpty()) {
                    gitlabUri = URI.create(link + "&page=" + nextPage.get());
                } else {
                    nextPage = Optional.empty();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.of(result);
    }

    @Override
    public Optional<List<GitlabCommit>> getCommits(URI uri) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String encodedPath = getEncodedPath(uri.getPath());
        return getCommits(uri, user, repo, encodedPath, null);
    }

    @Override
    public Optional<List<GitlabCommit>> getCommits(URI uri, Instant datetime) {
        String user = getUserFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String encodedPath = getEncodedPath(uri.getPath());
        return getCommits(uri, user, repo, encodedPath, datetime);
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
    public String getUserFromUrl(URI uri)  {
        return uri.getPath().split("/")[1];
    }

    @Override
    public String getRepoFromUrl(URI uri)  {
        return uri.getPath().split("/")[2];
    }

    @Override
    public String getBranchFromUrl(URI uri)  {
        return uri.getPath().split("/")[5];
    }

    @Override
    public String getEncodedPath(String url) {
        String[] segments = url.split("/");
        return String.join("/", Arrays.copyOfRange(segments, 6, segments.length));
    }
}
