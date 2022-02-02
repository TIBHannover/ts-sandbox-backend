package eu.tib.ts.service.impl;

import lombok.SneakyThrows;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.FileInputStream;

public class OntologyFileData {
    private static final String PATH = "src/test/resources/dices.owl";
    private static final String RDF_XML = "TTL";
    protected static final OWLOntology OWL_ONTOLOGY;
    protected static final OntModel ONT_MODEL;

    static {
        OWL_ONTOLOGY = getOwlOntology();
        ONT_MODEL = getOntModel();
    }

    @SneakyThrows
    private static OWLOntology getOwlOntology() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        return manager.loadOntologyFromOntologyDocument(new File(PATH));
    }

    @SneakyThrows
    private static OntModel getOntModel() {
        FileInputStream in = new FileInputStream(PATH);
        OntModel model = ModelFactory.createOntologyModel();
        model.read(in, null, RDF_XML);

        return model;
    }
}
