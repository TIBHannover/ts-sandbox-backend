package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.service.ProcessedOntologyService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OntologyController.class)
class OntologyControllerTest {
    @MockBean
    ProcessedOntologyService ontologyService;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testGetOntologyList() {
        when(ontologyService.getOntologies())
            .thenReturn(getOntologyDtos());

        mockMvc.perform(
                get("/api/ontology/list")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0].ontologyId", is("ontologyId0")))
            .andExpect(jsonPath("$.[1].ontologyId", is("ontologyId1")))
            .andExpect(jsonPath("$", hasSize(2)))
            .andReturn();
    }

    @SneakyThrows
    @Test
    void testGetOntologyIdList() {
        when(ontologyService.getOntologyIds())
            .thenReturn(List.of("0", "1", "2", "3"));

        mockMvc.perform(
                get("/api/ontology/ids")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[0]", is("0")))
            .andExpect(jsonPath("$.[1]", is("1")))
            .andExpect(jsonPath("$", hasSize(4)))
            .andReturn();
    }

    private static List<OntologyDto> getOntologyDtos() {
        OntologyDto ontologyDto0 = OntologyDto.builder()
            .ontologyId("ontologyId0")
            .uri("http://purl.obolibrary.org/obo/ro000000")
            .build();

        OntologyDto ontologyDto1 = OntologyDto.builder()
            .ontologyId("ontologyId1")
            .uri("http://purl.obolibrary.org/obo/ro000001")
            .build();

        return List.of(ontologyDto0, ontologyDto1);
    }
}
