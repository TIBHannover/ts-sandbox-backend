package eu.tib.ontologyhistory.utils;

import org.obolibrary.robot.IOHelper;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.IOException;

public class OntologyUtils {

    private OntologyUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static OWLOntology loadOntology(File file) throws IOException {
        IOHelper ioHelper = new IOHelper();
        return ioHelper.loadOntology(file);
    }
}