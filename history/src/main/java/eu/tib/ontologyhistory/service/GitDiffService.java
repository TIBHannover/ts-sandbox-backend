package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.model.GitDiff;
import eu.tib.ontologyhistory.repository.GitDiffRepository;
import eu.tib.ontologyhistory.repository.RobotRepository;
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
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class GitDiffService {

    private static final Path ONTOLOGY_LEFT = Path.of("ontology-left.txt");

    private static final Path ONTOLOGY_RIGHT = Path.of("ontology-right.txt");

    private final GitDiffRepository gitDiffRepository;

    public void create(String url) {
        GitService<?> gitService = GitServiceFactory.getService(url);

        val diffAdds = gitService.getDiffAdds(url);
        for (val diffAdd : diffAdds) {
            try {
                val diff = makeDiff(Files.write(ONTOLOGY_LEFT, diffAdd.gitRawFileLeft().getBytes()),
                        Files.write(ONTOLOGY_RIGHT, diffAdd.gitRawFileRight().getBytes()));
                val gitDiff = GitDiff.builder()
                        .sha(diffAdd.sha())
                        .url(url)
                        .parentSha(diffAdd.parentSha())
                        .diff(diff)
                        .build();

                gitDiffRepository.insert(gitDiff);

            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    public String findBySha(String sha) {
        val gitDiff = gitDiffRepository.findFirstBySha(sha);
        if (gitDiff != null) {
            return gitDiff.getDiff();
        }
        return "";
    }

    public GitDiff findFirstByUrl(String url) {
        val gitDiff = gitDiffRepository.findFirstByUrl(url);
        return gitDiff;
    }

    public void deleteAll() {
        gitDiffRepository.deleteAll();
    }

    public void deleteAllByUrl(String url) {
        gitDiffRepository.deleteAllByUrl(url);
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
