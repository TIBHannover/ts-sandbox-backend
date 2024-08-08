package eu.tib.ontologyhistory.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
@Slf4j
public class GitDiffService {

    public String makeDiff(Path left, Path right) {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "--no-index", left.toString(), right.toString());
        String result = "";
        try {
            Process process = processBuilder.start();

            result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return result;
    }

}
