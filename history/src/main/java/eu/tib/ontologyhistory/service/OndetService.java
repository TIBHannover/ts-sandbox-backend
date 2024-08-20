package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.DiffDtoTimeline;
import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.conto.GraphInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class OndetService {

    private final RobotService robotService;

    private final ContoService contoService;

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

        val markdown = robotDiff == null ? null : robotDiff.markdown();

        return new DifferenceMarkdown(markdown, contoDiff, robotDiff.gitDiff());
    }

    public Optional<DiffDtoTimeline> findFirstByUrl(String url, String dataset) {

        val robotDiff = robotService.findFirstByUrl(url);
        if (robotDiff != null) {
            return Optional.of(new DiffDtoTimeline(robotDiff, Collections.emptyList()));
        }

        val contoDiff = contoService.findFirstByUrl(url, dataset);
        if (!contoDiff.isEmpty()) {
            return Optional.of(new DiffDtoTimeline(null, contoDiff));
        }

        return Optional.empty();
    }

    public Optional<DiffDtoTimeline> create(String url, String dataset) {
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
    }

    public void update(String id) {
        robotService.update(id);
        contoService.update(id);
    }
}
