package eu.tib.ts.service;

import org.apache.jena.ontology.OntModel;

import java.util.Set;

public interface OntologyTraverseService {
    Set<String> getImports(OntModel model);

    Set<String> getNamespaces(OntModel model);

    Set<String> getProperties(OntModel model);

    Set<String> getClasses(OntModel model);
}
