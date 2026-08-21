package eu.tib.ontologyhistory.service;

import com.google.gson.JsonParser;
import eu.tib.ontologyhistory.dto.DiffAvailability;
import eu.tib.ontologyhistory.dto.DiffAvailabilityStatus;
import eu.tib.ontologyhistory.dto.DiffAvailabilitySummary;
import eu.tib.ontologyhistory.dto.DiffDtoTimeline;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.ProcessedDiffTimelineItem;
import eu.tib.ontologyhistory.dto.conto.Difference;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;
import eu.tib.ontologyhistory.model.*;
import eu.tib.ontologyhistory.repository.BatchProcessingJobRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OndetService {

    private final RobotService robotService;
    private final ContoService contoService;
    private final GitDiffService gitDiffService;
    private final BatchProcessingJobRepository batchProcessingJobRepository;

    @Value("${ondet.batch.conto.enabled:false}")
    private boolean batchContoEnabled;

    @Value("${ondet.batch.conto.max-diff-pairs:5}")
    private int batchContoMaxDiffPairs;

    @Value("${ondet.batch.conto.max-pair-raw-bytes:5000000}")
    private int batchContoMaxPairRawBytes;

    @PostConstruct
    public void markStaleRunningBatchJobs() {
        val runningJobs = batchProcessingJobRepository.findAllByStatus(BatchJobStatus.RUNNING);
        for (val job : runningJobs) {
            job.setStatus(BatchJobStatus.FAILED);
            job.setFinishedAt(Instant.now());
            job.setMessage("Batch job was interrupted by backend restart before completion");
            for (val result : job.getResults()) {
                if (result.getStatus() == BatchOntologyStatus.PROCESSING) {
                    result.setStatus(BatchOntologyStatus.FAILED);
                    result.setFinishedAt(Instant.now());
                    result.setMessage("Ontology processing was interrupted by backend restart");
                    job.setProcessed(job.getProcessed() + 1);
                    job.setNotAdded(job.getNotAdded() + 1);
                }
            }
            batchProcessingJobRepository.save(job);
        }
    }

    public Set<URI> findAll() {
        val result = new HashSet<>(robotService.findAllUrls());
        result.addAll(gitDiffService.findAllUrls());
        return result;
    }

    public Set<URI> findAllAsync() {
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
        return find(sha, dataset, true);
    }

    public DifferenceMarkdown find(String sha, String dataset, boolean includeGitDiff) {
        return find(sha, dataset, includeGitDiff, 1_000_000);
    }

    public DifferenceMarkdown find(String sha, String dataset, boolean includeGitDiff, int maxGitDiffBytes) {
        val robotDiff = robotService.findByParentSha(sha);
        val gitDiff = includeGitDiff ? gitDiffService.findByParentSha(sha, maxGitDiffBytes) : "";
        val gitDiffUrl = gitDiffService.findRemoteDiffUrlByParentSha(sha);
        val gitDiffSizeBytes = gitDiffService.findDiffSizeBytesByParentSha(sha);
        val contoDiff = contoService.timeline(sha, dataset);
        val robotMarkdown = robotDiff == null ? new Document() : robotDiff.markdown();
        val gitStatus = gitStatus(gitDiff, gitDiffUrl, includeGitDiff, gitDiffSizeBytes, maxGitDiffBytes);
        val status = new DiffAvailabilitySummary(
                robotStatus(robotDiff, robotMarkdown, gitStatus),
                contoStatus(contoDiff, gitStatus),
                gitStatus
        );

        return new DifferenceMarkdown(robotMarkdown, contoDiff, gitDiff, gitDiffUrl, status);
    }

    private DiffAvailability robotStatus(DiffDto robotDiff, Document robotMarkdown, DiffAvailability gitStatus) {
        if (robotDiff != null && robotDiff.error() != null && !robotDiff.error().isBlank()) {
            return new DiffAvailability(DiffAvailabilityStatus.NOT_AVAILABLE, robotDiff.error(), null, null, null, robotDiff.errorCode());
        }
        if (robotMarkdown != null && robotMarkdown.getString("file") != null && !robotMarkdown.getString("file").isBlank()) {
            return new DiffAvailability(DiffAvailabilityStatus.AVAILABLE, "ROBOT diff is available", null, null, null, null);
        }
        if (gitStatus.status() == DiffAvailabilityStatus.NOT_APPLICABLE) {
            return new DiffAvailability(DiffAvailabilityStatus.NOT_APPLICABLE,
                    "ROBOT diff is not applicable for this selected commit because it is not a stored adjacent ontology-file version. The ontology file may not have existed yet, may not have changed in this commit, or this commit was not part of the processed diff pairs.",
                    null, null, null, null);
        }
        return new DiffAvailability(DiffAvailabilityStatus.NOT_AVAILABLE,
                "ROBOT diff was not stored for this commit. The diff may have failed, timed out, or exceeded processing limits.",
                null, null, null, null);
    }

    private DiffAvailability contoStatus(Difference contoDiff, DiffAvailability gitStatus) {
        if (contoDiff != null && contoDiff.error() != null && !contoDiff.error().isBlank()) {
            return new DiffAvailability(DiffAvailabilityStatus.NOT_AVAILABLE, contoDiff.error(), null, null, null, null);
        }
        if (contoDiff != null && contoDiff.changes() != null && !contoDiff.changes().isEmpty()) {
            return new DiffAvailability(DiffAvailabilityStatus.AVAILABLE, "COnto diff is available", null, null, null, null);
        }
        if (gitStatus.status() == DiffAvailabilityStatus.NOT_APPLICABLE) {
            return new DiffAvailability(DiffAvailabilityStatus.NOT_APPLICABLE,
                    "COnto diff is not applicable for this selected commit because it is not a stored adjacent ontology-file version.",
                    null, null, null, null);
        }
        return new DiffAvailability(DiffAvailabilityStatus.NOT_AVAILABLE,
                "COnto diff was not stored for this commit. The diff may have failed, timed out, or produced no queryable result.",
                null, null, null, null);
    }

    private DiffAvailability gitStatus(String gitDiff, String gitDiffUrl, boolean includeGitDiff,
                                       Integer gitDiffSizeBytes, int maxGitDiffBytes) {
        if (gitDiffSizeBytes != null && gitDiffSizeBytes <= maxGitDiffBytes) {
            return new DiffAvailability(DiffAvailabilityStatus.AVAILABLE,
                    includeGitDiff && gitDiff != null && !gitDiff.isBlank()
                            ? "Syntax diff is available inline."
                            : "Syntax diff is small enough to load inline.",
                    gitDiffUrl, gitDiffSizeBytes, true, null);
        }
        if (gitDiffSizeBytes != null && gitDiffUrl != null && !gitDiffUrl.isBlank()) {
            return new DiffAvailability(DiffAvailabilityStatus.EXTERNAL_URL,
                    "Syntax diff is too large to load inline. Open it in the source repository compare view.",
                    gitDiffUrl, gitDiffSizeBytes, false, null);
        }
        if (includeGitDiff && gitDiff != null && !gitDiff.isBlank()) {
            return new DiffAvailability(DiffAvailabilityStatus.AVAILABLE, "Syntax diff is available", null, gitDiffSizeBytes, true, null);
        }
        if (gitDiffUrl != null && !gitDiffUrl.isBlank()) {
            return new DiffAvailability(DiffAvailabilityStatus.EXTERNAL_URL,
                    "Syntax diff can be opened in the source repository compare view.",
                    gitDiffUrl, gitDiffSizeBytes, false, null);
        }
        return new DiffAvailability(DiffAvailabilityStatus.NOT_APPLICABLE,
                "Syntax diff is not stored for this selected commit. It may not correspond to an adjacent ontology-file version, the ontology file may not have existed yet, or the file may not have changed in this commit.",
                null, gitDiffSizeBytes, false, null);
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
        val diffAdds = getDiffAdds(uri);
        log.warn("Processing ontology {} with {} commit diff pair(s)", uri, diffAdds.size());
        if (diffAdds.isEmpty()) {
            return null;
        }

        return create(uri, diffAdds, dataset);
    }

    private DiffDtoTimeline create(URI uri, List<DiffAdd> diffAdds, String dataset) {
        robotService.deleteAllByUrl(uri);
        gitDiffService.deleteAllByUrl(uri);

        gitDiffService.create(uri, diffAdds);
        robotService.create(uri, diffAdds);
        contoService.create(uri, diffAdds, dataset);

        return findFirstByUrl(uri, dataset);
    }


    public DiffDtoTimeline createAsync(URI uri, String dataset) {
        List<DiffAdd> diffAdds;
        try {
            diffAdds = getDiffAdds(uri);
        } catch (Exception e) {
            return null;
        }
        if (diffAdds.isEmpty()) {
            return null;
        }

        robotService.deleteAllByUrl(uri);
        gitDiffService.deleteAllByUrl(uri);

        val gitDiffFutures = CompletableFuture.runAsync(() -> gitDiffService.create(uri, diffAdds));
        val robotDiffFutures = CompletableFuture.runAsync(() -> robotService.create(uri, diffAdds));
        CompletableFuture.allOf(gitDiffFutures, robotDiffFutures).join();
        contoService.create(uri, diffAdds, dataset);


        return findFirstByUrl(uri, dataset);
    }

    public Map<String, List<String>> createAsync(List<URI> uris, String dataset) {
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

    public String createBatchJob(List<URI> uris, String dataset) {
        val jobId = UUID.randomUUID().toString();
        val safeUris = uris == null ? Collections.<URI>emptyList() : uris;
        val job = BatchProcessingJob.builder()
                .id(jobId)
                .status(BatchJobStatus.RUNNING)
                .total(safeUris.size())
                .processed(0)
                .added(0)
                .notAdded(0)
                .startedAt(Instant.now())
                .results(new ArrayList<>())
                .message("Batch processing started")
                .build();

        batchProcessingJobRepository.save(job);
        CompletableFuture.runAsync(() -> processBatchJob(jobId, safeUris, dataset));
        return jobId;
    }

    public BatchProcessingJob findBatchJob(String jobId) {
        return batchProcessingJobRepository.findById(jobId).orElse(null);
    }

    private void processBatchJob(String jobId, List<URI> uris, String dataset) {
        for (URI uri : uris) {
            val result = BatchOntologyResult.builder()
                    .uri(uri)
                    .status(BatchOntologyStatus.PROCESSING)
                    .startedAt(Instant.now())
                    .build();
            addBatchResult(jobId, result);

            try {
                if (!GitServiceType.isUriSupported(uri)) {
                    finishBatchResult(jobId, result, BatchOntologyStatus.UNSUPPORTED_HOST,
                            "Ontology URI is not a supported raw Git file URL", 0);
                    continue;
                }

                val diffAdds = getDiffAdds(uri);
                if (diffAdds.isEmpty()) {
                    finishBatchResult(jobId, result, BatchOntologyStatus.NO_COMPARABLE_VERSIONS,
                            "No comparable downloadable ontology-file versions were found for this file. The Git provider may have returned file-history commits where the file is missing at that commit/path.",
                            0);
                    continue;
                }

                val resultMessage = createForBatch(uri, diffAdds, dataset);
                if (resultMessage == null) {
                    finishBatchResult(jobId, result, BatchOntologyStatus.FAILED,
                            "Diff pair(s) were found, but no diff records were created", diffAdds.size());
                    continue;
                }

                val status = resultMessage.warning() == null
                        ? BatchOntologyStatus.ADDED
                        : BatchOntologyStatus.ADDED_WITH_WARNINGS;
                finishBatchResult(jobId, result, status, resultMessage.message(), diffAdds.size());
            } catch (Exception e) {
                log.error("Batch processing failed for ontology {}", uri, e);
                finishBatchResult(jobId, result, BatchOntologyStatus.FAILED,
                        e.getMessage(), null);
            }
        }

        completeBatchJob(jobId);
    }

    private void addBatchResult(String jobId, BatchOntologyResult result) {
        val job = findBatchJob(jobId);
        if (job == null) {
            return;
        }
        job.getResults().add(result);
        batchProcessingJobRepository.save(job);
    }

    private BatchCreateResult createForBatch(URI uri, List<DiffAdd> diffAdds, String dataset) {
        robotService.deleteAllByUrl(uri);
        gitDiffService.deleteAllByUrl(uri);

        gitDiffService.create(uri, diffAdds);
        robotService.create(uri, diffAdds);

        val warning = getBatchContoSkipReason(diffAdds);
        if (warning == null) {
            contoService.create(uri, diffAdds, dataset);
            return new BatchCreateResult("Ontology processed", null);
        }

        log.warn("Skipping COnto for ontology {} during batch: {}", uri, warning);
        return new BatchCreateResult("Ontology processed; " + warning, warning);
    }

    private String getBatchContoSkipReason(List<DiffAdd> diffAdds) {
        if (!batchContoEnabled) {
            return "COnto batch processing is disabled";
        }
        if (diffAdds.size() > batchContoMaxDiffPairs) {
            return String.format("COnto skipped because %d diff pairs exceed configured limit %d",
                    diffAdds.size(), batchContoMaxDiffPairs);
        }
        val largestPairBytes = diffAdds.stream()
                .mapToInt(this::rawPairBytes)
                .max()
                .orElse(0);
        if (largestPairBytes > batchContoMaxPairRawBytes) {
            return String.format("COnto skipped because largest raw pair is %d bytes and limit is %d",
                    largestPairBytes, batchContoMaxPairRawBytes);
        }
        return null;
    }

    private int rawPairBytes(DiffAdd diffAdd) {
        return byteLength(diffAdd.gitRawFileLeft()) + byteLength(diffAdd.gitRawFileRight());
    }

    private int byteLength(String value) {
        return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
    }

    private record BatchCreateResult(String message, String warning) {
    }

    private void finishBatchResult(String jobId, BatchOntologyResult result, BatchOntologyStatus status,
                                   String message, Integer diffPairCount) {
        val job = findBatchJob(jobId);
        if (job == null) {
            return;
        }
        val storedResult = job.getResults().stream()
                .filter(item -> Objects.equals(item.getUri(), result.getUri())
                        && item.getStatus() == BatchOntologyStatus.PROCESSING)
                .reduce((first, second) -> second)
                .or(() -> job.getResults().stream()
                        .filter(item -> Objects.equals(item.getUri(), result.getUri()))
                        .reduce((first, second) -> second))
                .orElse(result);

        storedResult.setStatus(status);
        storedResult.setMessage(message);
        storedResult.setDiffPairCount(diffPairCount);
        storedResult.setFinishedAt(Instant.now());

        job.setProcessed(job.getProcessed() + 1);
        if (status == BatchOntologyStatus.ADDED || status == BatchOntologyStatus.ADDED_WITH_WARNINGS) {
            job.setAdded(job.getAdded() + 1);
        } else {
            job.setNotAdded(job.getNotAdded() + 1);
        }
        batchProcessingJobRepository.save(job);
    }

    private void completeBatchJob(String jobId) {
        val job = findBatchJob(jobId);
        if (job == null) {
            return;
        }
        job.setFinishedAt(Instant.now());
        job.setStatus(job.getNotAdded() == 0 ? BatchJobStatus.COMPLETED : BatchJobStatus.COMPLETED_WITH_FAILURES);
        job.setMessage("Batch processing finished");
        batchProcessingJobRepository.save(job);
    }

    @Async
    public CompletableFuture<Map<String, List<String>>> createBatchAsync(List<URI> uris, String dataset) {
        return CompletableFuture.supplyAsync(() -> createAsync(uris, dataset));
    }

    public List<DiffAdd> getDiffAdds(URI uri) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val diffAdds = gitService.getDiffAdds(uri, null);
        log.warn("Found {} diff pair(s) for ontology {}", diffAdds.size(), uri);
        return diffAdds;
    }

    public List<DiffAdd> getDiffAdds(URI uri, Instant datetime) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val diffAdds = gitService.getDiffAdds(uri, datetime);
        log.warn("Found {} diff pair(s) for ontology {} since {}", diffAdds.size(), uri, datetime);
        return diffAdds;
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
        // TODO check if update by id (probably commit sha) is still needed
    }

    public void update(URI uri, Instant datetime, String dataset) {
        val diffAdds = getDiffAdds(uri, datetime);

        gitDiffService.create(uri, diffAdds);
        robotService.create(uri, diffAdds);
        contoService.create(uri, diffAdds, dataset);
    }

    @Async
    public void updateAsync(URI uri, Instant datetime, String dataset) {
        val diffAdds = getDiffAdds(uri, datetime);

        val gitDiffFutures = CompletableFuture.runAsync(() -> gitDiffService.createAsync(uri, diffAdds));
        val robotDiffFutures = CompletableFuture.runAsync(() -> robotService.createAsync(uri, diffAdds));
        val contoFutures = CompletableFuture.runAsync(() -> contoService.create(uri, diffAdds, dataset));

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

    public List<ProcessedDiffTimelineItem> getProcessedTimeline(URI uri) {
        val timelineByHeadSha = new LinkedHashMap<String, ProcessedDiffTimelineItem>();

        for (val robotDiff : robotService.findAllByUrl(uri)) {
            val headSha = firstNonBlank(robotDiff.parentSha(), robotDiff.sha());
            if (headSha == null) {
                continue;
            }
            timelineByHeadSha.put(headSha, new ProcessedDiffTimelineItem(
                    robotDiff.uri(),
                    headSha,
                    robotDiff.sha(),
                    firstNonNull(robotDiff.parentDatetime(), robotDiff.datetime()),
                    robotDiff.datetime(),
                    robotDiff.message(),
                    robotDiff.processingStatus(),
                    robotDiff.errorCode(),
                    false
            ));
        }

        for (val gitDiff : gitDiffService.findAllByUrl(uri)) {
            val headSha = firstNonBlank(gitDiff.parentSha(), gitDiff.sha());
            if (headSha == null) {
                continue;
            }
            val existing = timelineByHeadSha.get(headSha);
            timelineByHeadSha.put(headSha, new ProcessedDiffTimelineItem(
                    gitDiff.uri(),
                    headSha,
                    gitDiff.sha(),
                    firstNonNull(existing == null ? null : existing.date(), gitDiff.datetime()),
                    existing == null ? null : existing.parentDate(),
                    firstNonBlank(existing == null ? null : existing.message(), gitDiff.message()),
                    existing == null ? null : existing.robotStatus(),
                    existing == null ? null : existing.robotErrorCode(),
                    true
            ));
        }

        return timelineByHeadSha.values().stream()
                .sorted(this::compareTimelineItemsNewestFirst)
                .toList();
    }

    private int compareTimelineItemsNewestFirst(ProcessedDiffTimelineItem left, ProcessedDiffTimelineItem right) {
        if (left.date() == null && right.date() == null) {
            return 0;
        }
        if (left.date() == null) {
            return 1;
        }
        if (right.date() == null) {
            return -1;
        }
        return right.date().compareTo(left.date());
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private Instant firstNonNull(Instant first, Instant second) {
        return first == null ? second : first;
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
                .filter(GitServiceType::isUriSupported)
                .collect(Collectors.toList());
    }
}
