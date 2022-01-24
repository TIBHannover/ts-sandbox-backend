package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.PairwiseSimilarityModelAssembler;
import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.CharacteristicsInfo;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.ExternalOntology;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
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
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SimilarityController.class)
@Import({SimilarityModelAssembler.class, PairwiseSimilarityModelAssembler.class})
class SimilarityControllerTest {
    static final Pageable PAGEABLE = PageRequest.of(0, 2);

    @MockBean
    SimilarityService similarityService;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testGetSimilarityByPropertyInternal() {
        when(similarityService.getSimilarities(anyList(), any(CharacteristicsType.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPage());

        mockMvc.perform(
                get("/api/ontology/similarity/property/internal")
                    .param("ids", "dicl, dicob")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].name", is("name0")))
            .andExpect(jsonPath("$._embedded.similarities[1].name", is("name1")))
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
        when(similarityService.getSimilarities(
            any(ExternalOntology.class), any(CharacteristicsType.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPage());

        String body = "{\n" +
            "\t\"ontologyId\" : \"dicl\",\n" +
            "\t\"uri\": \"\",\n" +
            "\t\"properties\": [\"property0\", \"property1\"],\n" +
            "\t\"classes\": [\"class0\", \"class1\"],\n" +
            "\t\"imports\": [\"import0\", \"import1\"],\n" +
            "\t\"namespaces\": [\"namespace0\", \"namespace1\"]\n" +
            "}";

        mockMvc.perform(post("/api/ontology/similarity/property/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].name", is("name0")))
            .andExpect(jsonPath("$._embedded.similarities[1].name", is("name1")))
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

    @SneakyThrows
    @Test
    void testGetPairwiseSimilarityForInternalOntology() {
        when(similarityService.getPairwiseSimilarity(any(Optional.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPairwisePage());

        mockMvc.perform(
                get("/api/ontology/similarity/pairwise/internal")
                    .param("ids", "dicl, dicob")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].pair.first", is("ont0")))
            .andExpect(jsonPath("$._embedded.similarities[0].pair.second", is("ont1")))
            .andExpect(jsonPath("$._embedded.similarities", hasSize(2)))
            .andReturn();
    }

    @SneakyThrows
    @Test
    void testGetPairwiseSimilarityForExternalOntology() {
        when(similarityService.getPairwiseSimilarity(
            any(ExternalOntology.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPairwisePage());

        String body = "{\n" +
            "\t\"ontologyId\" : \"dicl\",\n" +
            "\t\"uri\": \"\",\n" +
            "\t\"properties\": [\"property0\", \"property1\"],\n" +
            "\t\"classes\": [\"class0\", \"class1\"],\n" +
            "\t\"imports\": [\"import0\", \"import1\"],\n" +
            "\t\"namespaces\": [\"namespace0\", \"namespace1\"]\n" +
            "}";

        mockMvc.perform(post("/api/ontology/similarity/pairwise/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].pair.first", is("ont0")))
            .andExpect(jsonPath("$._embedded.similarities[0].pair.second", is("ont1")))
            .andExpect(jsonPath("$._embedded.similarities", hasSize(2)))
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
            .name("name0")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        Similarity similarity1 = Similarity.builder()
            .name("name1")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        Similarity similarity2 = Similarity.builder()
            .name("name2")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        Similarity similarity3 = Similarity.builder()
            .name("name3")
            .ontologies(List.of(ontologyDto0, ontologyDto1))
            .build();

        return new PageImpl<>(List.of(similarity0, similarity1, similarity2, similarity3), PAGEABLE, 3);
    }

    private static Page<PairwiseSimilarity> createPairwisePage() {

        PairwiseSimilarity pairwiseSimilarity0 = PairwiseSimilarity.builder()
            .pair(Pair.of("ont0", "ont1"))
            .sum(100.0)
            .totalSum(100.0)
            .percent(100.0)
            .characteristics(
                Map.of(
                    CharacteristicsType.PROPERTY.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("prop1", "prop2")),
                    CharacteristicsType.IMPORT.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("import1", "import2")),
                    CharacteristicsType.CLASS.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("class1", "class2")),
                    CharacteristicsType.NAMESPACE.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("namespace1", "namespace2"))
                )
            )
            .build();

        PairwiseSimilarity pairwiseSimilarity1 = PairwiseSimilarity.builder()
            .pair(Pair.of("ont0", "ont2"))
            .sum(100.0)
            .totalSum(100.0)
            .percent(100.0)
            .characteristics(
                Map.of(
                    CharacteristicsType.PROPERTY.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("prop1", "prop2")),
                    CharacteristicsType.IMPORT.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("import1", "import2")),
                    CharacteristicsType.CLASS.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("class1", "class2")),
                    CharacteristicsType.NAMESPACE.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("namespace1", "namespace2"))
                )
            )
            .build();

        return new PageImpl<>(List.of(pairwiseSimilarity0, pairwiseSimilarity1), PAGEABLE, 1);
    }
}
