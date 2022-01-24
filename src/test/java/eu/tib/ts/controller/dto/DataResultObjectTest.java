package eu.tib.ts.controller.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataResultObjectTest {

    @Test
    void test_constructor() {
        OntologyDto dto = OntologyDto.builder()
            .ontologyId("ont1")
            .uri("http://purl.obolibrary.org/obo/ro000000")
            .build();

        DataResultObject<OntologyDto> dataResultObject = new DataResultObject<>(dto);

        assertEquals(dto, dataResultObject.getData());
    }

    @Test
    void test_builder() {
        OntologyDto dto = OntologyDto.builder()
            .ontologyId("ont1")
            .uri("http://purl.obolibrary.org/obo/ro000000")
            .build();

        DataResultObject<OntologyDto> dataResultObject = DataResultObject.<OntologyDto>builder()
            .data(dto)
            .build();

        assertEquals(dto, dataResultObject.getData());
    }
}
