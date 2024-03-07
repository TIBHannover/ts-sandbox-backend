package eu.tib.ts.service;

import org.apache.jena.ontology.OntModel;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OntologyStorageService {

OWLOntology loadOntologyIntoOWLOntologyFromMultipartFile(MultipartFile file) throws IOException, OWLOntologyCreationException;

OntModel loadOntologyIntoOntModelFromMultipartFile(MultipartFile file) throws IOException;

}
