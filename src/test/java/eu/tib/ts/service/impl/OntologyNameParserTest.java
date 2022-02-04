package eu.tib.ts.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OntologyNameParserTest {
    private OntologyNameParser nameParserService;

    @BeforeEach
    void setUp() {
        nameParserService = new OntologyNameParser();
    }

    @Test
    void testGetName_shouldReturnName() {
        String uri0 = "https://raw.githubusercontent.com/LiUSemWeb/Materials-Design-Ontology/master/mdo-full.owl";
        String uri1 = "https://raw.githubusercontent.com/tibonto/dr/master/DigitalReference.ttl";
        String uri2 = "https://raw.githubusercontent.com/Data-Semantics-Laboratory/Modular-Ontology-Modeling-for-Food-Supply-Chains/master/food-traceability-schema.ttl";

        assertEquals("mdo-full", nameParserService.getName(uri0));
        assertEquals("DigitalReference", nameParserService.getName(uri1));
        assertEquals("food-traceability-schema", nameParserService.getName(uri2));
    }

    @Test
    void testGetName_shouldReturnNull() {
        String uri0 = "someTestString";
        String uri1 = "some/Test/String";
        String uri2 = "some-test.string";

        assertNull(nameParserService.getName(uri0));
        assertNull(nameParserService.getName(uri1));
        assertNull(nameParserService.getName(uri2));
    }
}
