package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.OntologyType;
import eu.tib.ts.service.OntologyStorageService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class OntologyStorageServiceImpl implements OntologyStorageService {

    @Override
    public OWLOntology loadOntologyIntoOWLOntologyFromMultipartFile(MultipartFile file) throws IOException, OWLOntologyCreationException {

        InputStream in = file.getInputStream();

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(in);

        log.info("owlOntology.getOntologyID() :  " + owlOntology.getOntologyID());

        in.close();

    return owlOntology;

    }

    @Override
    public OntModel loadOntologyIntoOntModelFromMultipartFile(MultipartFile file) throws IOException {

        OntModel  model = ModelFactory.createOntologyModel();
        InputStream in = file.getInputStream();

        /**
         * TURTLE language only
         */
        model.read(in,null, "TURTLE");

        return model;

    }


}
