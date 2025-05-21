package eu.tib.ontologyhistory.service;

import com.google.gson.JsonParser;
import eu.tib.ontologyhistory.dto.DiffDtoTimeline;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;
import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.service.network.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OndetService {

    private final RobotService robotService;
    private final ContoService contoService;
    private final GitDiffService gitDiffService;

    public Set<URI> findAll() {
        val result = new HashSet<URI>();
        CompletableFuture<Set<URI>> robotFuture = CompletableFuture.supplyAsync(robotService::findAllUrls);
        CompletableFuture<Set<URI>> gitDiffFuture = CompletableFuture.supplyAsync(gitDiffService::findAllUrls);

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(robotFuture, gitDiffFuture);

        try {
            allFuture.get();
            result.addAll(robotFuture.get());
            result.addAll(gitDiffFuture.get());
        } catch (ExecutionException e) {
            log.error("Error while fetching ontologies: ", e);
        } catch (InterruptedException e) {
            log.error("Interrupted!", e);
            Thread.currentThread().interrupt();
        }

        return result;
    }

    public DifferenceMarkdown find(String sha, String dataset) {
        val robotDiff = robotService.findByParentSha(sha);
        val gitDiff = gitDiffService.findByParentSha(sha);
        val contoDiff = contoService.timeline(sha, dataset);

        if (robotDiff == null) {
            return new DifferenceMarkdown(new Document(), contoDiff, gitDiff);
        }

        return new DifferenceMarkdown(robotDiff.markdown(), contoDiff, gitDiff);
    }

    public DiffDtoTimeline findFirstByUrl(URI uri, String dataset) {

        val gitDiff = gitDiffService.findFirstByUrl(uri);
        if (gitDiff != null) {
            return new DiffDtoTimeline(null, Collections.emptyList(), gitDiff);
        }

        val robotDiff = robotService.findFirstByUrl(uri);
        if (robotDiff != null) {
            return new DiffDtoTimeline(robotDiff, Collections.emptyList(), null);
        }

        val contoDiff = contoService.findFirstByUrl(uri, dataset);
        if (!contoDiff.isEmpty()) {
            return new DiffDtoTimeline(null, contoDiff, null);
        }

        return null;
    }

    public DiffDtoTimeline create(URI uri, String dataset) {
        robotService.deleteAllByUrl(uri);
        gitDiffService.deleteAllByUrl(uri);

        val diffAdds = getDiffAdds(uri);
        gitDiffService.create(uri, diffAdds);
        robotService.create(uri, diffAdds);
        contoService.create(uri, dataset, diffAdds);

        return findFirstByUrl(uri, dataset);
    }


    public DiffDtoTimeline createAsync(URI uri, String dataset) {
        robotService.deleteAllByUrl(uri);
        gitDiffService.deleteAllByUrl(uri);

        List<DiffAdd> diffAdds;
        try {
            diffAdds = getDiffAdds(uri);
        } catch (Exception e) {
            return null;
        }

        val gitDiffFutures = CompletableFuture.runAsync(() -> gitDiffService.create(uri, diffAdds));
        val robotDiffFutures = CompletableFuture.runAsync(() -> robotService.create(uri, diffAdds));
        val contoFutures = CompletableFuture.runAsync(() -> contoService.create(uri, dataset, diffAdds));

        CompletableFuture.allOf(gitDiffFutures, robotDiffFutures, contoFutures).join();

        return findFirstByUrl(uri, dataset);
    }

    public Map<String, List<String>> create(List<URI> uris, String dataset) {
        val result = new HashMap<String, List<String>>();
        val addedList = new ArrayList<String>();
        val notAddedList = new ArrayList<String>();
        for (URI uri : uris) {
            if (createAsync(uri, dataset) != null) {
                addedList.add(String.valueOf(uri));
            } else {
                notAddedList.add(String.valueOf(uri));
            }
        }
        result.put("added", addedList);
        result.put("notAdded", notAddedList);
        return result;
    }

    @Async
    public CompletableFuture<Map<String, List<String>>> createBatchAsync(List<URI> uris, String dataset) {
        return CompletableFuture.supplyAsync(() -> create(uris, dataset));
    }

    public List<DiffAdd> getDiffAdds(URI uri) {
        GitService<?> gitService = GitServiceType.createService(uri);

        return gitService.getDiffAdds(uri, null);
    }

    public List<DiffAdd> getDiffAdds(URI uri, Instant datetime) {
        GitService<?> gitService = GitServiceType.createService(uri);

        return gitService.getDiffAdds(uri, datetime);
    }

    public void remove(String id) {
        robotService.deleteById(id);
        contoService.remove(id);
    }

    public void removeAll() {
        robotService.deleteAll();
        contoService.deleteAll();
        gitDiffService.deleteAll();
    }

    public void removeAllByUrl(URI uri) {
        robotService.deleteAllByUrl(uri);
        gitDiffService.deleteAllByUrl(uri);
    }

    public void update(String id) {
        robotService.update(id);
        contoService.update(id);
    }

    public void updateByUrl(URI uri, Instant datetime, String dataset) {
        robotService.updateByUrl(uri, datetime);
        contoService.updateByUrl(uri, datetime, dataset);
        gitDiffService.updateByUrl(uri, datetime);
    }

    @Async
    public void updateByUrlAsync(URI uri, Instant datetime, String dataset) {
        val diffAdds = getDiffAdds(uri, datetime);

        val gitDiffFutures = CompletableFuture.runAsync(() -> gitDiffService.create(uri, diffAdds));
        val robotDiffFutures = CompletableFuture.runAsync(() -> robotService.create(uri, diffAdds));
        val contoFutures = CompletableFuture.runAsync(() -> contoService.create(uri, dataset, diffAdds));

        CompletableFuture.allOf(gitDiffFutures, robotDiffFutures, contoFutures).join();
    }

    public List<? extends Commit> getCommits(URI uri) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val result = gitService.getCommits(uri);
        if (result != null) {
            return result;
        }
        return Collections.emptyList();
    }

    public GitDiffDto getVersion(URI uri) {
        val gitDiffs = gitDiffService.findAllByUrl(uri);

        if (gitDiffs != null && !gitDiffs.isEmpty()) {
            return gitDiffs.get(gitDiffs.size() - 1);
        }
        return null;
    }

    public Map<String, List<String>> resHistory(URI uri, Instant datetime, String resourceIRI) {
        return robotService.resHistory(uri, datetime, resourceIRI);
    }

    public List<URI> getTSOntologies() {
        HttpRequest getOntologies = HttpRequest.newBuilder()
                .uri(URI.create("https://api.terminology.tib.eu/api/v2/ontologies?size=1000"))
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(getOntologies, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                val parsedResponseBody = JsonParser.parseString(response.body());
                val jsonObject = parsedResponseBody.getAsJsonObject();
                val array = jsonObject.get("elements").getAsJsonArray();
                return array.asList().stream()
                        .filter(e -> e.getAsJsonObject().has("versioned_url"))
                        .map(item -> URI.create(item.getAsJsonObject().get("versioned_url").getAsString()))
                        .toList();
            } else {
                return Collections.emptyList();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted with the response: " + e);
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.error("IOException happened: " + e);
        }
        return Collections.emptyList();
    }

    public List<URI> filterUnsupportedOntologyTypes(List<URI> uris) {
        return uris.stream()
                .filter(GitServiceType::isHostSupported)
                .collect(Collectors.toList());
    }
}
