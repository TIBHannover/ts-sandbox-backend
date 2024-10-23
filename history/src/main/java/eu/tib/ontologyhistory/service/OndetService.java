package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.DiffDtoTimeline;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.conto.GraphInfo;
import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;
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

    public Set<TempGraph> findAll(String dataset) {
        val robotDiffs = robotService.findAllUrls();
        val gitDiffs = gitDiffService.findAllUrls();
//        val contoDiffs = contoService.findAll(dataset);
//        val result = new HashSet<GraphInfo>();
//        names.addAll(contoDiffs);

//        for (val name : names) {
//            val gitDiff = gitDiffService.findFirstByOrderByDatetimeDesc(name);
//            GitService<?> gitService = GitServiceFactory.getService(name);
//            val commits = gitService.getCommits(URI.create(name), gitDiff.datetime());
//            if (commits.isPresent()) {
//                result.add(new GraphInfo(name, gitDiff.datetime(), commits.get().size()));
//            } else {
//                result.add(new GraphInfo(name, gitDiff.datetime(), -1));
//            }
//        }

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
        robotService.deleteAllByUrl(url);
        gitDiffService.deleteAllByUrl(url);

        val diffAdds = getDiffAdds(url);
        gitDiffService.create(url, diffAdds);
        robotService.create(url, diffAdds);
        contoService.create(url, dataset, diffAdds);

        return findFirstByUrl(url, dataset);
    }

    public List<DiffAdd> getDiffAdds(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        return gitService.getDiffAdds(url);
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

    public void removeAllByUrl(String url) {
        robotService.deleteAllByUrl(url);
        gitDiffService.deleteAllByUrl(url);
//       not used currently
//       contoService.deleteAllByUrl(url);
    }

    public void update(String id) {
        robotService.update(id);
        contoService.update(id);
    }

    public void updateByUrl(String url, Instant datetime, String dataset) {
        robotService.updateByUrl(url, datetime);
        contoService.updateByUrl(url, datetime, dataset);
        gitDiffService.updateByUrl(url, datetime);
    }

    public List<?> getCommits(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val result = gitService.getCommits(URI.create(url));
        if (result.isPresent()) {
            return result.get();
        }
        return Collections.emptyList();
    }

    public GitDiffDto getVersion(String url) {
        val gitDiffs = gitDiffService.findAllByUrl(url);

        if (gitDiffs != null && !gitDiffs.isEmpty()) {
            return gitDiffs.get(gitDiffs.size() - 1);
        }
        return GitDiffDto.defaultValue();
    }

    public Map<String, List<String>> resHistory(String url, Instant datetime, String resourceIRI) {
        return robotService.resHistory(url, datetime, resourceIRI);
    }
}
