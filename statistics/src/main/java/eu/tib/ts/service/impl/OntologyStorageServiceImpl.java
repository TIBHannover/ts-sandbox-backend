package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyStorageService;

import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Path;

public class OntologyStorageServiceImpl implements OntologyStorageService {


    private final Path path;

    public OntologyStorageServiceImpl(Path path) {

        this.path = path;

    }

    @Override
    public OWLOntology storeOntology(MultipartFile file) {

        if (file.isEmpty()) {

        throw new RuntimeException("Failed to store empty file");

        }

    return null;

    }
}
