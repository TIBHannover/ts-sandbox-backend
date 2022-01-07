package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.ExternalOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.service.SimilarityService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SimilarityController.class)
@Import({SimilarityModelAssembler.class})
class SimilarityControllerTest {
    static final Pageable PAGEABLE = PageRequest.of(0, 2);

    @MockBean
    private SimilarityService similarityService;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testGetSimilarityByPropertyInternal() {
        when(similarityService.getSharedPropertyUri(anyList(), any(Pageable.class)))
            .thenReturn(createPage());

        String body = "[{\"ontologyId\" : \"dicl\",\"uri\" : \"\"},\n" +
            "{\"ontologyId\" : \"dicob\",\"uri\" : \"\"}]";

        mockMvc.perform(get("/api/ontology/similarity/property/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].name", is("property0")))
            .andExpect(jsonPath("$._embedded.similarities[1].name", is("property1")))
            .andExpect(jsonPath("$._links.first.href",
                is("http://localhost/api/ontology/similarity/property/internal?page=0&size=2")))
            .andExpect(jsonPath("$._links.self.href",
                is("http://localhost/api/ontology/similarity/property/internal?page=0&size=2")))
            .andExpect(jsonPath("$._links.next.href",
                is("http://localhost/api/ontology/similarity/property/internal?page=1&size=2")))
            .andExpect(jsonPath("$._links.last.href",
                is("http://localhost/api/ontology/similarity/property/internal?page=1&size=2")))
            .andExpect(jsonPath("$._embedded.similarities[0].ontologies", hasSize(2)))
            .andReturn();
    }

    @SneakyThrows
    @Test
    void testGetSimilarityByPropertyExternal() {
        when(similarityService.getSharedPropertyUri(any(ExternalOntology.class), any(Pageable.class)))
            .thenReturn(createPage());

        String body = "{\n" +
            "\t\"ontologyId\" : \"dicl\",\n" +
            "\t\"uri\": \"\",\n" +
            "\t\"properties\": [\"property0\", \"property1\"],\n" +
            "\t\"classes\": [\"class0\", \"class1\"],\n" +
            "\t\"imports\": [\"import0\", \"import1\"],\n" +
            "\t\"namespaces\": [\"namespace0\", \"namespace1\"]\n" +
            "}";

        mockMvc.perform(get("/api/ontology/similarity/property/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].name", is("property0")))
            .andExpect(jsonPath("$._embedded.similarities[1].name", is("property1")))
            .andExpect(jsonPath("$._links.first.href",
                is("http://localhost/api/ontology/similarity/property/external?page=0&size=2")))
            .andExpect(jsonPath("$._links.self.href",
                is("http://localhost/api/ontology/similarity/property/external?page=0&size=2")))
            .andExpect(jsonPath("$._links.next.href",
                is("http://localhost/api/ontology/similarity/property/external?page=1&size=2")))
            .andExpect(jsonPath("$._links.last.href",
                is("http://localhost/api/ontology/similarity/property/external?page=1&size=2")))
            .andExpect(jsonPath("$._embedded.similarities[0].ontologies", hasSize(2)))
            .andReturn();
    }

    private static Page<Similarity> createPage() {
        OntologyDto ontologyDto0 = OntologyDto.builder()
            .ontologyId("ontologyId0")
            .uri("http://purl.obolibrary.org/obo/ro000000")
            .build();

        OntologyDto ontologyDto1 = OntologyDto.builder()
            .ontologyId("ontologyId1")
            .uri("http://purl.obolibrary.org/obo/ro000001")
            .build();

        Similarity similarity0 = Similarity.builder()
            .name("property0")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        Similarity similarity1 = Similarity.builder()
            .name("property1")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        Similarity similarity2 = Similarity.builder()
            .name("property2")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        Similarity similarity3 = Similarity.builder()
            .name("property3")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        return new PageImpl<>(List.of(similarity0, similarity1, similarity2, similarity3), PAGEABLE, 3);
    }

}
