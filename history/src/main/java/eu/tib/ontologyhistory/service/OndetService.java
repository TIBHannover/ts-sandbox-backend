package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.DiffDtoTimeline;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.conto.GraphInfo;
import eu.tib.ontologyhistory.service.network.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class OndetService {

    private final RobotService robotService;

    private final ContoService contoService;
    private final GitDiffService gitDiffService;

    public Set<GraphInfo> findAll(String dataset) {
        val robotDiffs = robotService.findAllUrls();
        val contoDiffs = contoService.findAll(dataset);

        val result = new HashSet<>(robotDiffs);
        result.addAll(contoDiffs);

        return result;
    }

    public DifferenceMarkdown find(String sha, String dataset) {
        val robotDiff = robotService.findBySha(sha);
        val contoDiff = contoService.timeline(sha, dataset);
        val gitDiff = gitDiffService.findBySha(sha);

        if (robotDiff == null) {
            return new DifferenceMarkdown(new Document(), contoDiff, gitDiff);
        }

        return new DifferenceMarkdown(robotDiff.markdown(), contoDiff, gitDiff);
    }

    public Optional<DiffDtoTimeline> findFirstByUrl(String url, String dataset) {

        val gitDiff = gitDiffService.findFirstByUrl(url);
        if (gitDiff != null) {
            return Optional.of(new DiffDtoTimeline(null, Collections.emptyList(), gitDiff));
        }

        val robotDiff = robotService.findFirstByUrl(url);
        if (robotDiff != null) {
            return Optional.of(new DiffDtoTimeline(robotDiff, Collections.emptyList(), null));
        }

        val contoDiff = contoService.findFirstByUrl(url, dataset);
        if (!contoDiff.isEmpty()) {
            return Optional.of(new DiffDtoTimeline(null, contoDiff, null));
        }

        return Optional.empty();
    }

    public Optional<DiffDtoTimeline> create(String url, String dataset) {
        gitDiffService.create(url);
        robotService.create(url);
        contoService.create(url, dataset);

        return findFirstByUrl(url, dataset);
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

    public void update(String id) {
        robotService.update(id);
        contoService.update(id);
    }

    public List<?> getCommits(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val result = gitService.getCommits(URI.create(url));
        if (result.isPresent()) {
            return result.get();
        }
        return Collections.emptyList();
    }
}
