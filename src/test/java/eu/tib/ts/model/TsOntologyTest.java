package eu.tib.ts.model;

import eu.tib.ts.model.ontology.Classification;
import eu.tib.ts.model.ontology.Config;
import eu.tib.ts.model.ontology.TsOntology;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TsOntologyTest {

    @Test
    void test() {
        Classification classification = Classification.builder()
            .collection(
                List.of(
                    "NFDI4ING",
                    "NFDI4PHYS"
                )
            )
            .dfg(
                List.of(
                    "201-02 Biophysics",
                    "32 Physics"
                )
            )
            .build();

        Config config = Config.builder()
            .id("https://raw.githubusercontent.com/physh-org/PhySH/master/physh.ttl")
            .versionIri("versionIri")
            .fileLocation("https://raw.githubusercontent.com/physh-org/PhySH/master/physh.ttl")
            .allowDownload(true)
            .title("PhySH - Physics Subject Headings")
            .namespace("physh")
            .preferredPrefix("physh")
            .description("PhySH (Physics Subject Headings) is a physics classification scheme developed by APS to organize journal, meeting, and other content by topic.")
            .homepage("https://physh.org/")
            .tracker("https://github.com/physh-org/PhySH/issues")
            .creators(List.of("American Physical Society (https://www.aps.org/)"))
            .reasonerType("NONE")
            .oboSlims(false)
            .hierarchicalProperties(
                List.of(
                    "http://www.w3.org/2004/02/skos/core#broader",
                    "https://physh.org/rdf/2018/01/01/core#inDiscipline",
                    "https://physh.org/rdf/2018/01/01/core#inFacet"
                )
            )
            .classifications(List.of(classification))
            .skos(false)
            .build();


        TsOntology tsOntology = TsOntology.builder()
            .ontologyId("ont1")
            .fileHash("a67c1869337e4412eeaed93ca557ac4f0fb77994")
            .loaded("loaded")
            .loadAttempts(1)
            .message("message")
            .numberOfIndividuals(2)
            .numberOfProperties(3)
            .numberOfTerms(4)
            .status("LOADED")
            .updated("2022-01-21T14:50:29.115+0000")
            .config(config)
            .build();

        assertEquals("ont1", tsOntology.getOntologyId());
        assertEquals("a67c1869337e4412eeaed93ca557ac4f0fb77994", tsOntology.getFileHash());
        assertEquals("loaded", tsOntology.getLoaded());
        assertEquals("message", tsOntology.getMessage());

        assertEquals(config, tsOntology.getConfig());
        assertNotNull(config.getId());
        assertNotNull(config.getFileLocation());
        assertNotNull(config.getPreferredPrefix());
        assertNotNull(config.getNamespace());
        assertNotNull(config.getDescription());
        assertNotNull(config.getHomepage());
        assertNotNull(config.getCreators());
        assertNotNull(config.getReasonerType());

        assertEquals(classification.getCollection(), config.getClassifications().get(0).getCollection());
        assertEquals(classification.getDfg(), config.getClassifications().get(0).getDfg());
    }
}
