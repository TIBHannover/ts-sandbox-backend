package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyTraverseService;
import lombok.SneakyThrows;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OntologyTraverseServiceImplTest {
    private static final String PATH = "src/test/resources/dices.owl";
    private static final String RDF_XML = "TTL";

    private OntologyTraverseService ontologyTraverseService;
    private OntModel model;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        ontologyTraverseService = new OntologyTraverseServiceImpl();

        FileInputStream in = new FileInputStream(PATH);
        model = ModelFactory.createOntologyModel();
        model.read(in, null, RDF_XML);
    }

    @Test
    void testGetClasses() {
        Set<String> actual = ontologyTraverseService.getClasses(model);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/contexts#context"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/energy#energyconsumptionstatistics"));
    }

    @Test
    void testGetProperties() {
        Set<String> actual = ontologyTraverseService.getProperties(model);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/entities#hasbuildingunit"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/entities#concretizesatalltimes"));
    }

    @Test
    void testGetNamespaces() {
        Set<String> actual = ontologyTraverseService.getNamespaces(model);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("http://purl.org/dc/elements/1.1/"));
        assertTrue(actual.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
    }

    @Test
    void testGetImports() {
        Set<String> actual = ontologyTraverseService.getImports(model);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Information/information.ttl"));
    }
}
