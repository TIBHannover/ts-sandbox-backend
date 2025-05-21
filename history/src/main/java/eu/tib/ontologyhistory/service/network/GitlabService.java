package eu.tib.ontologyhistory.service.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonParser;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitServiceRequest;
import eu.tib.ontologyhistory.model.gitlab.GitlabCommit;
import eu.tib.ontologyhistory.service.GitTokenType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class GitlabService implements GitService<GitlabCommit> {

    private static final String NO_GITLAB_TOKEN_SET = "gitlab_access_token_not_set";

    private String ACCESS_TOKEN;

    private String HOST;

    private static String GITLAB_REST_API_V4_BASE_URL;

    public GitlabService(GitTokenType tokenType, String host) {
        GITLAB_REST_API_V4_BASE_URL =  "https://" + host + "/api/v4/";
        HOST = host;
        try {
            ACCESS_TOKEN = System.getenv(tokenType.name());
            if (ACCESS_TOKEN == null) {
                log.warn("Gitlab-related token not set, using default");
                ACCESS_TOKEN = NO_GITLAB_TOKEN_SET;
            }
        } catch (NullPointerException e) {
            log.error("You tried to set the null value for {} environment variable: \n{}", tokenType, e);
        } catch (SecurityException e) {
            log.error("Security manager did not allow to get the value of {} environment variable: \n{}", tokenType, e);
        }
    }

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String GITLAB_REST_API_PROJECTS_CONTEXT = "projects/";

    @Override
    public List<DiffAdd> getDiffAdds(URI uri, Instant datetime) {
        val request = buildGitServiceObject(uri);

        List<GitlabCommit> commits = getCommits(uri, request, datetime);
        commits.sort(Comparator.comparing(GitlabCommit::committed_date));
        return new ArrayList<>(processCommits(uri, commits, request));
    }

    @Override
    public List<DiffAdd> processCommits(URI uri, List<GitlabCommit> commits, GitServiceRequest request) {
        List<DiffAdd> diffAdds = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ListIterator<GitlabCommit> iterator = commits.listIterator();
        GitlabCommit current = null;
        while (iterator.hasNext()) {
            val next = iterator.next();
            if (current != null) {
                GitlabCommit finalCurrent = current;
                futures.add(CompletableFuture.runAsync(() -> processCommitPair(uri, finalCurrent, next, request, diffAdds), executor));
            }
            current = next;
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return diffAdds;
    }

    @Override
    public void processCommitPair(URI uri, GitlabCommit commit, GitlabCommit parentCommit, GitServiceRequest request, List<DiffAdd> diffAdds) {
        String rawFile = getRawFileUrl(uri, request, commit.id());
        String parentRawFile = null;
        if (rawFile != null) {
            parentRawFile = getRawFileUrl(uri, request, parentCommit.id());
        }

        if (rawFile != null && parentRawFile != null) {
            DiffAdd diffAdd = new DiffAdd(
                    String.format("https://" + HOST + "/%s/-/raw/%s/%s", request.projectId(), commit.id(), request.path()),
                    String.format("https://" + HOST + "/%s/-/raw/%s/%s", request.projectId(), parentCommit.id(), request.path()),
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
    public String getRawFileUrl(URI uri, GitServiceRequest request, String sha) {

        String link = GITLAB_REST_API_V4_BASE_URL + GITLAB_REST_API_PROJECTS_CONTEXT + request.projectId() + "/repository/files/" + UriUtils.encode(request.path(), StandardCharsets.UTF_8) + "/raw?ref=" + sha;

        URI gitlabUri = URI.create(link);

        HttpRequest requestGetRawFile = buildHttpRequestCheckToken(gitlabUri);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(requestGetRawFile, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            log.error("Interrupted with the response: " + e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("IOException happened: " + e);
        }
        return null;
    }

    @Override
    public List<GitlabCommit> getCommits(URI uri) {
        val request = buildGitServiceObject(uri);
        return getCommits(uri, request, null);
    }

    @Override
    public List<GitlabCommit> getCommits(URI uri, GitServiceRequest request, Instant datetime) {

        String link = GITLAB_REST_API_V4_BASE_URL + GITLAB_REST_API_PROJECTS_CONTEXT + request.projectId() + "/repository/commits";

        if (request.path() != null) {
            link += "?path=" + request.path();
        }

        if (request.branch() != null) {
            link += "&ref_name=" + request.branch();
        }

        if (datetime != null) {
            link += "&since=" + datetime;
        }


        List<GitlabCommit> result = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();


        URI gitlabUri = URI.create(link);
        Optional<String> nextPage = Optional.of("init");

        while (nextPage.isPresent()) {
            HttpRequest httpRequest = buildHttpRequestCheckToken(gitlabUri);
            try {
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                List<GitlabCommit> commits = objectMapper.readValue(response.body(), new TypeReference<>() {});
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
    public String getOwnerFromUrl(URI uri) {
        return uri.getPath().split("/")[1];
    }

    @Override
    public String getRepoFromUrl(URI uri) {
        return uri.getPath().split("/")[2];
    }

    @Override
    public String getBranchFromUrl(URI uri) {
        String[] segments = uri.getPath().split("/");
        String branch = "undefined";
        for (int i = 1; i < segments.length; i++) {
            if (segments[i].equals("-") && segments[i + 1].equals("raw")) {
                return segments[i + 2];
            }
        }
        return branch;
    }

    @Override
    public String getEncodedPath(URI uri) {
        String[] segments = uri.getPath().split("/");
        String path = "undefined";
        for (int i = 1; i < segments.length; i++) {
            if (segments[i].equals("-") && segments[i + 1].equals("raw")) {
                return String.join("/", Arrays.copyOfRange(segments, i + 3, segments.length));
            }
        }
        return path;
    }

    public String getProjectId(URI uri) {
        val encodedProjectPath = getEncodedProjectPath(uri);
        String link = GITLAB_REST_API_V4_BASE_URL + GITLAB_REST_API_PROJECTS_CONTEXT + encodedProjectPath;
        HttpClient client = HttpClient.newHttpClient();
        URI gitlabUri = URI.create(link);
        HttpRequest httpRequest = buildHttpRequestCheckToken(gitlabUri);

        try {
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            val parsedResponseBody = JsonParser.parseString(response.body());
            val jsonObject = parsedResponseBody.getAsJsonObject();
            return jsonObject.get("id").getAsString();
        } catch (InterruptedException e) {
            log.warn("Interrupted: {}", String.valueOf(e));
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("IoException during receiving of project ID: {}", String.valueOf(e));
        }
        return null;
    }

    private String getEncodedProjectPath(URI uri) {
        String[] segments = uri.getPath().split("/");
        StringBuilder projectId = new StringBuilder();
        for(int i = 1; i < segments.length; i++) {
            if (!segments[i+1].equals("-")) {
                projectId.append(segments[i]).append("%2F");
            } else {
                projectId.append(segments[i]);
                break;
            }
        }
        return projectId.toString();
    }

    private GitServiceRequest buildGitServiceObject(URI uri) {
        String projectId = getProjectId(uri);
        String branch = getBranchFromUrl(uri);
        String encodedPath = getEncodedPath(uri);

        return GitServiceRequest.builder()
                .projectId(projectId)
                .branch(branch)
                .path(encodedPath)
                .build();
    }

    private HttpRequest buildHttpRequestCheckToken(URI uri) {
        val requestBuilder = HttpRequest.newBuilder()
                .uri(uri);

        if (!ACCESS_TOKEN.equals(NO_GITLAB_TOKEN_SET)) {
            requestBuilder.header("Authorization", "Bearer " + ACCESS_TOKEN);
        }

        return requestBuilder.build();
    }
}
