package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;
import eu.tib.ontologyhistory.mapper.GittDiffMapper;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.GitDiff;
import eu.tib.ontologyhistory.repository.GitDiffRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class GitDiffService {

    private static final Path ONTOLOGY_LEFT = Path.of("ontology-left.txt");

    private static final Path ONTOLOGY_RIGHT = Path.of("ontology-right.txt");

    private final GitDiffRepository gitDiffRepository;

    private final GittDiffMapper gittDiffMapper;

    public void create(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url);
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, url);
        }
    }

    public void create(String url, List<DiffAdd> diffAdds) {

        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, url);
        }
    }

    public void updateByUrl(String url, Instant datetime) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url, datetime);
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, url);
        }
    }

    public String findBySha(String sha) {
        val gitDiff = gitDiffRepository.findFirstBySha(sha);
        if (gitDiff != null) {
            return gitDiff.getDiff();
        }
        return "";
    }

    public String findByParentSha(String parentSha) {
        val gitDiff = gitDiffRepository.findFirstByParentSha(parentSha);
        if (gitDiff != null) {
            return gitDiff.getDiff();
        }
        return "";
    }

    public GitDiffDto findFirstByUrl(String url) {
        val gitDiff = gitDiffRepository.findFirstByUrl(url);
        return gittDiffMapper.entityToDto(gitDiff);
    }

    public Set<TempGraph> findAllUrls() {
        val gitDiffs = gitDiffRepository.findAll();
        val urls = new HashSet<TempGraph>();
        for (GitDiff item : gitDiffs) {
            urls.add(new TempGraph(item.getUrl()));
        }
        return urls;
    }

    public List<GitDiffDto> findAllByUrl(String url) {
        val gitDiffs = gitDiffRepository.findAllByUrl(url);
        gitDiffs.sort(Comparator.comparing(GitDiff::getDatetime));

        return gittDiffMapper.entityToDto(gitDiffs);
    }

    public GitDiffDto findFirstByOrderByDatetimeDesc(String url) {
        val gitDiff = gitDiffRepository.findFirstByUrlOrderByDatetimeDesc(url);
        return gittDiffMapper.entityToDto(gitDiff);
    }

    public void deleteAll() {
        gitDiffRepository.deleteAll();
    }

    public void deleteAllByUrl(String url) {
        gitDiffRepository.deleteAllByUrl(url);
    }

    public void makeDiffFromGit(DiffAdd diffAdd, String url) {
        try {
            val diff = makeDiff(Files.write(ONTOLOGY_LEFT, diffAdd.gitRawFileLeft().getBytes()),
                    Files.write(ONTOLOGY_RIGHT, diffAdd.gitRawFileRight().getBytes()));

            val gitDiff = GitDiff.builder()
                    .url(url)
                    .sha(diffAdd.sha())
                    .parentSha(diffAdd.parentSha())
                    .diff(diff)
                    .datetime(diffAdd.parentDatetime())
                    .build();

            gitDiffRepository.insert(gitDiff);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String makeDiff(Path left, Path right) {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "--no-index", left.toString(), right.toString());
        try {
            Process process = processBuilder.start();

            return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return "We are sorry to inform you, but some exception happened during creation of git diff";
    }

}
