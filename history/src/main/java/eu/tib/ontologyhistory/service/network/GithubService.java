package eu.tib.ontologyhistory.service.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitServiceRequest;
import eu.tib.ontologyhistory.model.github.GithubCommit;
import eu.tib.ontologyhistory.service.GitTokenType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GithubService implements GitService<GithubCommit> {

    private static final String NO_GITHUB_TOKEN_SET = "github_access_token_not_set";

    private static String ACCESS_TOKEN;

    public GithubService(GitTokenType tokenType, String host) {
        try {
            ACCESS_TOKEN = System.getenv(tokenType.name());
            if (ACCESS_TOKEN == null) {
                log.warn("Github token not set, using default");
                ACCESS_TOKEN = NO_GITHUB_TOKEN_SET;
            }
        } catch (NullPointerException e) {
            log.error("You tried to set the null value for {} environment variable: \n{}", tokenType, e);
        } catch (SecurityException e) {
            log.error("Security manager did not allow to get the value of {} environment variable: \n{}", tokenType, e);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String GITHUB_NEXT_PAGE_REGEX = "(?<=<)(\\S*)(?=>; rel=\"next\")";

    @Override
    public List<DiffAdd> getDiffAdds(URI uri, Instant datetime) {
        val request = buildGitServiceObject(uri);

        List<GithubCommit> commits = getCommits(uri, request, datetime);
        Collections.reverse(commits);
        return new ArrayList<>(processCommits(uri, commits, request));
    }

    @Override
    public List<DiffAdd> processCommits(URI uri, List<GithubCommit> githubCommits, GitServiceRequest request) {
        List<DiffAdd> diffAdds = new ArrayList<>();
        ListIterator<GithubCommit> iterator = githubCommits.listIterator();
        GithubCommit current = null;
        while (iterator.hasNext()) {
            val next = iterator.next();
            if (current != null) {
                processCommitPair(uri, current, next, request, diffAdds);
            }
            current = next;
        }
        return diffAdds;
    }

    @Override
    public void processCommitPair(URI uri, GithubCommit githubCommit, GithubCommit parentGithubCommit, GitServiceRequest request, List<DiffAdd> diffAdds) {
        String rawFile = getRawFileUrl(uri, request, githubCommit.sha());
        String parentRawFile = getRawFileUrl(uri, request, parentGithubCommit.sha());

        if (rawFile != null && parentRawFile != null) {
            DiffAdd diffAdd = new DiffAdd(
                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", request.owner(), request.repo(), githubCommit.sha(), request.path()),
                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", request.owner(), request.repo(), parentGithubCommit.sha(), request.path()),
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
    public String getRawFileUrl(URI uri, GitServiceRequest request, String sha) {

        URI githubRawFileApi = UriComponentsBuilder.fromUri(uri)
                .host("raw.githubusercontent.com")
                .replacePath("/{owner}/{repo}/{sha}/{path}")
                .buildAndExpand(request.owner(), request.repo(), sha, request.path())
                .toUri();

        HttpRequest requestGetRawFile = buildHttpRequestCheckToken(githubRawFileApi);

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
    public List<GithubCommit> getCommits(URI uri) {
        val request = buildGitServiceObject(uri);
        return getCommits(uri, request, null);
    }

    @Override
    public List<GithubCommit> getCommits(URI uri, GitServiceRequest request, Instant datetime) {
        Pattern pattern = Pattern.compile(GITHUB_NEXT_PAGE_REGEX);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(uri)
                .host("api.github.com")
                .replacePath("/repos/{owner}/{repo}/commits");

        uriBuilder.queryParam("sha", request.branch());

        if (request.path() != null) {
            uriBuilder.queryParam("path", request.path());
        }

        if (datetime != null) {
            uriBuilder.queryParam("since", datetime);
        }

        URI githubApiUri = uriBuilder
                .buildAndExpand(request.owner(), request.repo())
                .toUri();

        Optional<String> nextPage = Optional.of("init");
        List<GithubCommit> result = new ArrayList<>();

        while (nextPage.isPresent()) {
            HttpRequest httpRequest = buildHttpRequestCheckToken(githubApiUri);

            try {
                val commits = new ArrayList<GithubCommit>();
                        HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                val responseBody = JsonParser.parseString(response.body());
                if (responseBody.isJsonArray()) {
                    commits.addAll(objectMapper.readValue(response.body(), new TypeReference<>() {}));
                }
                if (responseBody.isJsonObject()) {
                    JsonObject jsonObject = responseBody.getAsJsonObject();
                    if (jsonObject.get("message").getAsString().equals("Moved Permanently")) {
                        httpRequest = buildHttpRequestCheckToken(URI.create(jsonObject.get("url").getAsString()));

                        response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                        commits.addAll(objectMapper.readValue(response.body(), new TypeReference<>() {}));
                    }
                }
                result.addAll(commits);

                nextPage = response.headers().firstValue("link");
                if (nextPage.isPresent() && nextPage.get().contains("rel=\"next\"")) {
                    Matcher matcher = pattern.matcher(nextPage.get());
                    if (matcher.find()) {
                        githubApiUri = URI.create(matcher.group());
                    }
                } else {
                    nextPage = Optional.empty();
                }
            } catch (InterruptedException e) {
                log.error("Interrupted with the response: " + e);
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                log.error("IOException happened: " + e);
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

    private GitServiceRequest buildGitServiceObject(URI uri) {
        String owner = getOwnerFromUrl(uri);
        String repo = getRepoFromUrl(uri);
        String branch = getBranchFromUrl(uri);
        String encodedPath = getEncodedPath(uri);

        return GitServiceRequest.builder()
                .owner(owner)
                .repo(repo)
                .branch(branch)
                .path(encodedPath)
                .build();
    }

    private HttpRequest buildHttpRequestCheckToken(URI uri) {
        val requestBuilder = HttpRequest.newBuilder()
                .uri(uri);

        if (!ACCESS_TOKEN.equals(NO_GITHUB_TOKEN_SET)) {
            requestBuilder.header("Authorization", "Bearer " + ACCESS_TOKEN);
        }

        return requestBuilder.build();
    }
}
