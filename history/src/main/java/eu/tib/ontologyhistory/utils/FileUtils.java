package eu.tib.ontologyhistory.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static File createTempFile(String fileName, String content) throws IOException {
        Path filePath = Path.of(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
}