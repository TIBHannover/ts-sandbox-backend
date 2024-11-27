package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.exception.DecryptionException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class GitSecretService {

    private GitSecretService() {
        throw new IllegalStateException("Utility class");
    }

    public static String decrypt(String fileName) {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "secret", "cat", fileName);
        try {
            Process process = processBuilder.start();
            process.waitFor();
            log.info("Process finished with exit code {}", process.exitValue());
            log.info("Process.getInputStream(): {}", IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8));
            return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DecryptionException("Failed to decrypt file: " + fileName, e);
        }
    }
}
