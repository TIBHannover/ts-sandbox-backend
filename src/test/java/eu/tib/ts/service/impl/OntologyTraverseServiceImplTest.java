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
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Variables#Unit"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Processes#Resource"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Information#ProjectInformationModel"));
    }

    @Test
    void testGetProperties() {
        Set<String> actual = ontologyTraverseService.getProperties(ONT_MODEL);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Contexts#dependsOn"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Entities#memberPartOf"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Entities#memberPartOfAtAllTimes"));
    }

    @Test
    void testGetNamespaces() {
        Set<String> actual = ontologyTraverseService.getNamespaces(OWL_ONTOLOGY);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("http://purl.org/dc/elements/1.1/"));
        assertTrue(actual.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Information#"));
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Energy#"));
    }

    @Test
    void testGetImports() {
        Set<String> actual = ontologyTraverseService.getImports(OWL_ONTOLOGY);

        assertTrue(actual.size() > 0);
        assertTrue(actual.contains("https://w3id.org/digitalconstruction/0.5/Information/information.ttl"));
    }
}
