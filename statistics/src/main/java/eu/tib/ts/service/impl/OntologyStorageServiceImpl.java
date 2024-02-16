package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyStorageService;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class OntologyStorageServiceImpl implements OntologyStorageService {

    private final Path path;

    public OntologyStorageServiceImpl(Path path) throws IOException {

        this.path = path;

    }

    @Override
    public OWLOntology getOWLOntologyFromMultipartFile(MultipartFile file) throws IOException {

        OWLOntology owlOntology;

        if (file.isEmpty()) {

        throw new RuntimeException("Failed to store empty file");

        } else {

            owlOntology = null;

            Path filePath = this.path.resolve(Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();

            if (!filePath.getParent().equals(this.path.toAbsolutePath())) {

                throw new RuntimeException("Can not store file outside of current folder");
            }

            try (InputStream inputStream = file.getInputStream()) {

                File ontologyFile = new File(file.toString());

                copyInputStreamToFile(inputStream, ontologyFile);

//              Files.copy(inputStream,filePath, StandardCopyOption.REPLACE_EXISTING);

                OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

                try {

                    owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(ontologyFile);

                } catch (OWLOntologyCreationException e) {

                    e.printStackTrace();
                }
            }
        }

    return owlOntology;

    }

    /**
     * copy input streamn to a file
     *
     * @param inputStream
     * @param file
     * @throws IOException
     */
    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[10096];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

}
