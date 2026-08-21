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

    private static final int MAX_MONGO_DOCUMENT_BYTES = 15_000_000;

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
        val gitDiff = findByParentShaOrSha(parentSha);
        if (gitDiff != null) {
            return gitDiff.getDiff();
        }
        return "";
    }

    public String findByParentSha(String parentSha, int maxBytes) {
        val gitDiff = findByParentShaOrSha(parentSha);
        if (gitDiff == null || gitDiff.getDiff() == null) {
            return "";
        }
        if (gitDiff.getDiff().getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            return "";
        }
        return gitDiff.getDiff();
    }

    public Integer findDiffSizeBytesByParentSha(String parentSha) {
        val gitDiff = findByParentShaOrSha(parentSha);
        if (gitDiff == null || gitDiff.getDiff() == null) {
            return null;
        }
        return gitDiff.getDiff().getBytes(StandardCharsets.UTF_8).length;
    }

    public String findRemoteDiffUrlByParentSha(String parentSha) {
        val gitDiff = findByParentShaOrSha(parentSha);
        if (gitDiff == null) {
            return "";
        }

        return buildRemoteDiffUrl(gitDiff.getUri(), gitDiff.getSha(), gitDiff.getParentSha());
    }

    private GitDiff findByParentShaOrSha(String sha) {
        val gitDiff = gitDiffRepository.findFirstByParentSha(sha);
        if (gitDiff != null) {
            return gitDiff;
        }
        return gitDiffRepository.findFirstBySha(sha);
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

            if (diff.getBytes(StandardCharsets.UTF_8).length > MAX_MONGO_DOCUMENT_BYTES) {
                log.warn("Skipping Git diff for ontology {} commit {} because diff is larger than MongoDB document limit",
                        uri, diffAdd.parentSha());
                ontLeft.toFile().delete();
                ontRight.toFile().delete();
                return;
            }

            val gitDiff = GitDiff.builder()
                    .uri(uri)
                    .sha(diffAdd.sha())
                    .parentSha(diffAdd.parentSha())
                    .diff(diff)
                    .message(diffAdd.messageRight())
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

    private String buildRemoteDiffUrl(URI uri, String baseSha, String headSha) {
        if (uri == null || uri.getHost() == null || baseSha == null || headSha == null) {
            return "";
        }

        if ("raw.githubusercontent.com".equals(uri.getHost())) {
            String[] segments = uri.getPath().split("/");
            if (segments.length > 3) {
                return String.format("https://github.com/%s/%s/compare/%s...%s",
                        segments[1], segments[2], baseSha, headSha);
            }
        }

        if (uri.getHost().equals("gitlab.com")
                || uri.getHost().equals("git.rwth-aachen.de")
                || uri.getHost().equals("git.tib.eu")
                || uri.getHost().equals("labs.etsi.org")) {
            String[] segments = uri.getPath().split("/");
            StringBuilder projectPath = new StringBuilder();
            for (int i = 1; i < segments.length; i++) {
                if (i + 1 < segments.length && "-".equals(segments[i]) && "raw".equals(segments[i + 1])) {
                    break;
                }
                if (!projectPath.isEmpty()) {
                    projectPath.append("/");
                }
                projectPath.append(segments[i]);
            }

            if (!projectPath.isEmpty()) {
                return String.format("https://%s/%s/-/compare/%s...%s",
                        uri.getHost(), projectPath, baseSha, headSha);
            }
        }

        return "";
    }

}
