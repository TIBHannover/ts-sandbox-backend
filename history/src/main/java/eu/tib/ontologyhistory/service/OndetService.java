package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.DifferenceMarkdown;
import eu.tib.ontologyhistory.dto.diff.DiffDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OndetService {

    private final RobotService robotService;

    private final ContoService contoService;

    public List<?> findAll(String dataset) {
        val robotDiffs = robotService.findAll();
        val ondetDiffs = contoService.findAll(dataset);

        return new ArrayList<>();
    }

    public DifferenceMarkdown find(String sha, String dataset) {
        val robotDiff = robotService.findBySha(sha);
        val contoDiff = contoService.timeline(sha, dataset);

        val markdown = robotDiff == null ? null : robotDiff.markdown();

        return new DifferenceMarkdown(markdown, contoDiff, robotDiff.gitDiff());
    }

    public List<DiffDto> findByUrl(String url) {

        val robotDiffs = robotService.findAllByUrl(url);
//        val contoDiffs = contoService.findByUrl(url);

        return robotDiffs;
    }

    public void create(String url, String dataset) {
        robotService.create(url);
        contoService.create(url, dataset);
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
