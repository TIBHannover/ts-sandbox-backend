package eu.tib.ontologyhistory.service;

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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class GitDiffService {

    private final GitDiffRepository gitDiffRepository;

    private final GittDiffMapper gittDiffMapper;

    public void create(URI uri) {
        GitService<?> gitService = GitServiceType.createService(uri);

        val diffAdds = gitService.getDiffAdds(uri, null);
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, uri);
        }
    }

    public void create(URI uri, List<DiffAdd> diffAdds) {
        for (val diffAdd : diffAdds) {
            makeDiffFromGit(diffAdd, uri);
        }
    }

    public void createAsync(URI uri, List<DiffAdd> diffAdds) {
        diffAdds.forEach(diffAdd -> CompletableFuture.runAsync(() -> makeDiffFromGit(diffAdd, uri)));
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

    public void deleteAll() {
        gitDiffRepository.deleteAll();
    }

    public void deleteAllByUrl(URI uri) {
        gitDiffRepository.deleteAllByUri(uri);
    }

    public void makeDiffFromGit(DiffAdd diffAdd, URI uri) {
        try {
            val ontLeft = Files.createTempFile("left-file", ".txt");
            val ontRight = Files.createTempFile("right-file", ".txt");
            val diff = makeDiff(Files.write(ontLeft, diffAdd.gitRawFileLeft().getBytes()),
                    Files.write(ontRight, diffAdd.gitRawFileRight().getBytes()));

            val gitDiff = GitDiff.builder()
                    .uri(uri)
                    .sha(diffAdd.sha())
                    .parentSha(diffAdd.parentSha())
                    .diff(diff)
                    .datetime(diffAdd.parentDatetime())
                    .build();

            gitDiffRepository.insert(gitDiff);

            ontLeft.toFile().delete();
            ontRight.toFile().delete();

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
