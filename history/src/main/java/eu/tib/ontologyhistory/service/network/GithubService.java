package eu.tib.ontologyhistory.service.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.ontology.OntologyDto;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.github.Commit;
import eu.tib.ontologyhistory.service.DiffService;
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
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class GithubService {

    private DiffService diffService;

    public List<Diff> create(OntologyDto ontologyDto) throws Exception {
        val uri = checkUriValidity(ontologyDto.url());
        if (uri.isPresent()) {
            val user = getUserFromUrl(uri.get());
            val repo = getRepoFromUrl(uri.get());
            val encodedPath = getEncodedPath(uri.get().getPath());

            val commits = getCommits(uri.get(), user, repo, encodedPath);

            if (commits.isPresent()) {
                Iterator<Commit> iterator = commits.get().iterator();
                val diffs = new ArrayList<Diff>();
                while (iterator.hasNext()) {
                    val commit = iterator.next();
                    if (iterator.hasNext()) {
                        val parentCommit = iterator.next();

                        val rawFile = getRawFileUrl(uri.get(), user, repo, commit.sha(), encodedPath);
                        val parentRawFile = getRawFileUrl(uri.get(), user, repo, parentCommit.sha(), encodedPath);

                        if (rawFile.isPresent() && parentRawFile.isPresent()) {
                            val diffAdd = new DiffAdd(String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, repo, commit.sha(), encodedPath),
                                    String.format("https://raw.githubusercontent.com/%s/%s/%s/%s", user, repo, commit.sha(), encodedPath),
                                    rawFile.get(),
                                    parentRawFile.get(),
                                    commit.sha(),
                                    commit.sha(),
                                    commit.commit().committer().date(),
                                    parentCommit.commit().committer().date(),
                                    commit.commit().committer().date(),
                                    commit.commit().message()
                            );

                            val diff = diffService.makeDiffFromGit(diffAdd);
                            if (diff != null) {
                                diffs.add(diff);
                            }
                        }

                    }
                }
                return diffs;
            }
         }
        return Collections.emptyList();
    }

    public Optional<String> getRawFileUrl(URI uri, String owner, String repo, String sha, String path) {

        URI githubRawFileApi = UriComponentsBuilder.fromUri(uri)
                .host("raw.githubusercontent.com")
                .replacePath("/{owner}/{repo}/{sha}/{path}")
                .buildAndExpand(owner, repo, sha, path)
                .toUri();

        HttpRequest requestGetRawFile = HttpRequest.newBuilder()
                .uri(githubRawFileApi)
                .header("Authorization", "Bearer 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")
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

    public Optional<List<Commit>> getCommits(URI uri, String owner, String repo, String path) {
        URI githubApiUri = UriComponentsBuilder.fromUri(uri)
                .host("api.github.com")
                .replacePath("/repos/{owner}/{repo}/commits")
                .queryParam("path", path)
                .buildAndExpand(owner, repo)
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(githubApiUri)
                .header("Authorization", "Bearer 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")
                .build();

            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                return Optional.of(objectMapper.readValue(response.body(), new TypeReference<>() {}));
            } catch (InterruptedException e) {
                log.error("Interrupted with the response: " + e);
                Thread.currentThread().interrupt();
            } catch (IOException e) { log.error("IOException happened: " + e); }
        return Optional.empty();
    }

    public Optional<URI> checkUriValidity(String url) {
        try {
            val uri = new URI(url);
            return Optional.of(uri);
        } catch (URISyntaxException uriSyntaxException) {
            log.error("URISyntaxException with the provided URL: " + url);
        }
        return Optional.empty();
    }

    private String getUserFromUrl(URI uri) {
        return uri.getPath().split("/")[1];
    }

    private String getRepoFromUrl(URI uri) {
        return uri.getPath().split("/")[2];
    }

    private String getBranchFromUrl(URI uri) {
        return uri.getPath().split("/")[5];
    }

    private String getEncodedPath(String url) {
        String[] segments = url.split("/");
        return String.join("/", Arrays.copyOfRange(segments, 4, segments.length));
    }

}
