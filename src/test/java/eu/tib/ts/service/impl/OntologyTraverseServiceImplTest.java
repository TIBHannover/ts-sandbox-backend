package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyTraverseService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OntologyTraverseServiceImplTest extends OntologyFileData {
    private OntologyTraverseService ontologyTraverseService;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        ontologyTraverseService = new OntologyTraverseServiceImpl();
    }

    @Test
    void testGetClasses() {
        Set<String> actual = ontologyTraverseService.getClasses(ONT_MODEL);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/variables#unit"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/processes#resource"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/information#projectinformationmodel"));
    }

    @Test
    void testGetProperties() {
        Set<String> actual = ontologyTraverseService.getProperties(ONT_MODEL);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/contexts#dependson"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/entities#memberpartof"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/entities#memberpartofatalltimes"));
    }

    @Test
    void testGetNamespaces() {
        Set<String> actual = ontologyTraverseService.getNamespaces(OWL_ONTOLOGY);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("http://purl.org/dc/elements/1.1/"));
        assertTrue(actual.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/information#"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/energy#"));
    }

    @Test
    void testGetImports() {
        Set<String> actual = ontologyTraverseService.getImports(OWL_ONTOLOGY);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/information/information.ttl"));
    }
}
