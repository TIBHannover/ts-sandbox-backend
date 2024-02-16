package eu.tib.ts.service;

import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.web.multipart.MultipartFile;

public interface OntologyStorageService {

OWLOntology storeOntology (MultipartFile file);

}
