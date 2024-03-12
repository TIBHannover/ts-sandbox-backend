package eu.tib.ts.service.impl;
import eu.tib.ts.service.OntologyStorageService;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Slf4j
@Service
public class OntologyStorageServiceImpl implements OntologyStorageService {

    @Override
    public OWLOntology loadOntologyIntoOWLOntologyFromMultipartFile(MultipartFile file) throws IOException, OWLOntologyCreationException {

        InputStream in = file.getInputStream();

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(in);

        in.close();

        return owlOntology;

    }

    @Override
    public OntModel loadOntologyIntoOntModelFromMultipartFile(MultipartFile file) throws IOException {

        OntModel  model = ModelFactory.createOntologyModel();
        InputStream in = file.getInputStream();

        log.info("MultipartFile: " + file);
        log.info("Input stream: " + in.toString());

        /**
         * TURTLE language only
         */
        model.read(in,"file:"+file.getResource().getURL(), "TTL");

        return model;

    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[20096];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }
}