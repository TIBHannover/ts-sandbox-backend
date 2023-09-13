package eu.tib.ontologyhistory.utils;

import eu.tib.ontologyhistory.model.Ontology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class FileUtils {
    public static void writeOntologyToFile(Ontology ontology, String fileName) throws IOException {
        Path ontologyPath = Paths.get(fileName);
        Files.write(ontologyPath, Collections.singleton(ontology.getDescription()));
    }
}
