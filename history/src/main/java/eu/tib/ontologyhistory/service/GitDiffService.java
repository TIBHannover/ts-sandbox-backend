package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.conto.TempGraph;
import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.dto.git.GitDiffDto;
import eu.tib.ontologyhistory.mapper.GittDiffMapper;
import eu.tib.ontologyhistory.model.GitDiff;
import eu.tib.ontologyhistory.repository.GitDiffRepository;
import eu.tib.ontologyhistory.service.network.GitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class GitDiffService {

    private static final Path ONTOLOGY_LEFT = Path.of("ontology-left.txt");

    private static final Path ONTOLOGY_RIGHT = Path.of("ontology-right.txt");

    private final GitDiffRepository gitDiffRepository;

    private final GittDiffMapper gittDiffMapper;

    public void create(URI uri) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

        val diffAdds = gitService.getDiffAdds(uri, null);
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, uri);
        }
    }

    public void create(URI uri, List<DiffAdd> diffAdds) {
        diffAdds.forEach(diffAdd -> CompletableFuture.runAsync(() -> makeDiffFromGit(diffAdd, uri)));
    }

    public void updateByUrl(URI uri, Instant datetime) {
        GitService<?> gitService = GitServiceFactory.getService(uri);

        val diffAdds = gitService.getDiffAdds(uri, datetime);
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, uri);
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

    public GitDiffDto findFirstByUrl(URI uri) {
        val gitDiff = gitDiffRepository.findFirstByUri(uri);
        return gittDiffMapper.entityToDto(gitDiff);
    }

    public Set<URI> findAllUrls() {
        return gitDiffRepository.findAllUris()
                .stream()
                .map(item -> URI.create(item.getString("uri")))
                .collect(Collectors.toSet());
    }

    public List<GitDiffDto> findAllByUrl(URI uri) {
        val gitDiffs = gitDiffRepository.findAllByUri(uri);
        gitDiffs.sort(Comparator.comparing(GitDiff::getDatetime));

        return gittDiffMapper.entityToDto(gitDiffs);
    }

    public GitDiffDto findFirstByOrderByDatetimeDesc(URI uri) {
        val gitDiff = gitDiffRepository.findFirstByUriOrderByDatetimeDesc(uri);
        return gittDiffMapper.entityToDto(gitDiff);
    }

    public void deleteAll() {
        gitDiffRepository.deleteAll();
    }

    public void deleteAllByUrl(URI uri) {
        gitDiffRepository.deleteAllByUri(uri);
    }

    public void makeDiffFromGit(DiffAdd diffAdd, URI uri) {
        try {
            val diff = makeDiff(Files.write(ONTOLOGY_LEFT, diffAdd.gitRawFileLeft().getBytes()),
                    Files.write(ONTOLOGY_RIGHT, diffAdd.gitRawFileRight().getBytes()));

            val gitDiff = GitDiff.builder()
                    .uri(uri)
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
