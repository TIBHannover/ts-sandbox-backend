package eu.tib.ts.service;

import org.apache.jena.ontology.OntModel;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OntologyStorageService {

OWLOntology loadOntologyIntoOWLOntologyFromMultipartFile(MultipartFile file) throws IOException;

OntModel loadOntologyIntoOntModelFromMultipartFile(MultipartFile file) throws IOException;

}
