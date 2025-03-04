package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.DiffDtoTimeline;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.conto.GraphInfo;
import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;
import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.service.network.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class OndetService {

    private final RobotService robotService;
    private final ContoService contoService;
    private final GitDiffService gitDiffService;

    public Set<TempGraph> findAll() {
        val robotDiffs = robotService.findAllUrls();
        val gitDiffs = gitDiffService.findAllUrls();

        val result = new HashSet<>(robotDiffs);
        result.addAll(gitDiffs);
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

        List<DiffAdd> diffAdds;
        try {
            diffAdds = getDiffAdds(uri);
        } catch (Exception e) {
            return null;
        }
        gitDiffService.create(uri, diffAdds);
        robotService.create(uri, diffAdds);
        contoService.create(uri, dataset, diffAdds);

        return findFirstByUrl(uri, dataset);
    }

    public Map<String, List<String>> create(List<URI> uris, String dataset) {
        val result = new HashMap<String, List<String>>();
        val addedList = new ArrayList<String>();
        val notAddedList = new ArrayList<String>();
        for (URI uri : uris) {
            if (create(uri, dataset) != null) {
                addedList.add(String.valueOf(uri));
            } else {
                notAddedList.add(String.valueOf(uri));
            }
        }
        result.put("added", addedList);
        result.put("notAdded", notAddedList);
        return result;
    }

    public List<DiffAdd> getDiffAdds(URI uri) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

        return gitService.getDiffAdds(uri, null);
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

    public List<? extends Commit> getCommits(URI uri) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

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
        return GitDiffDto.defaultValue();
    }

    public Map<String, List<String>> resHistory(URI uri, Instant datetime, String resourceIRI) {
        return robotService.resHistory(uri, datetime, resourceIRI);
    }
}
