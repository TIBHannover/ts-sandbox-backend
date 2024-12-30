package eu.tib.ts.assessments.model.tags.ontology;

import java.util.Set;

public interface ExtendedOntology extends Ontology {
    Set<String> getProperties();

    Set<String> getClasses();

    Set<String> getImports();

    Set<String> getNamespaces();

    Set<String> getIndividuals();

    Set<String> getCollection();
}
