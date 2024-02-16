package eu.tib.ts.service;

import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OntologyStorageService {

OWLOntology getOWLOntologyFromMultipartFile(MultipartFile file) throws IOException;


}
