package eu.tib.ts.service;

import org.apache.jena.ontology.OntModel;

public interface OntologyReadService {
    OntModel readOntology(String uri);
}
