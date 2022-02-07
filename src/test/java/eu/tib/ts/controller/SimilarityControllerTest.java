package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.PairwiseSimilarityModelAssembler;
import eu.tib.ts.controller.assember.SimilarityModelAssembler;
import eu.tib.ts.controller.dto.OntologyDto;
import eu.tib.ts.model.ontology.CharacteristicsInfo;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.service.PreProcessingOntologyService;
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
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    PreProcessingOntologyService preProcessingOntologyService;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testGetSimilarityByPropertyInternal() {
        when(similarityService.getSimilarities(anyList(), any(CharacteristicsType.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPage());

        mockMvc.perform(
                get("/api/ontology/similarity/property/internal/list")
                    .param("ids", "dicl, dicob")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.similarities[0].name", is("name0")))
            .andExpect(jsonPath("$._embedded.similarities[1].name", is("name1")))
            .andExpect(jsonPath("$._links.first.href",
                is("http://localhost/api/ontology/similarity/property/internal/list?page=0&size=2")))
            .andExpect(jsonPath("$._links.self.href",
                is("http://localhost/api/ontology/similarity/property/internal/list?page=0&size=2")))
            .andExpect(jsonPath("$._links.next.href",
                is("http://localhost/api/ontology/similarity/property/internal/list?page=1&size=2")))
            .andExpect(jsonPath("$._links.last.href",
                is("http://localhost/api/ontology/similarity/property/internal/list?page=1&size=2")))
            .andExpect(jsonPath("$._embedded.similarities[0].ontologies", hasSize(2)))
            .andReturn();
    }

    @SneakyThrows
    @Test
    void testGetSimilarityByPropertyExternal() {
        when(similarityService.getSimilarities(
            any(ProcessedOntology.class), any(CharacteristicsType.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPage());

        ProcessedOntology processedOntology0 = ProcessedOntology.builder()
            .id(0)
            .ontologyId("ontology_0")
            .uri("https://something0.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_20"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_20"))
            .build();

        when(preProcessingOntologyService.preProcess(any(Optional.class), anyString()))
            .thenReturn(processedOntology0);

        mockMvc.perform(
                get("/api/ontology/similarity/property/external")
                    .param("characteristicsType", "PROPERTY")
                    .param("url", "https://raw.githubusercontent.com/LiUSemWeb/Materials-Design-Ontology/master/mdo-full.owl")
                    .contentType(MediaType.APPLICATION_JSON)
            )
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
            .andExpect(jsonPath("$._embedded.similarities[0].sum", is(100.0)))
            .andExpect(jsonPath("$._embedded.similarities[0].totalSum", is(100.0)))
            .andExpect(jsonPath("$._embedded.similarities[0].percentage", is(100.0)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.import.size", is(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.import.list", hasSize(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.class.size", is(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.class.list", hasSize(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.namespace.size", is(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.namespace.list", hasSize(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.property.size", is(2)))
            .andExpect(jsonPath("$._embedded.similarities[0].characteristics.property.list", hasSize(2)))
            .andExpect(jsonPath("$._embedded.similarities", hasSize(2)))
            .andReturn();
    }

    @SneakyThrows
    @Test
    void testGetPairwiseSimilarityForExternalOntology() {
        when(similarityService.getPairwiseSimilarity(
            any(ProcessedOntology.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(createPairwisePage());

        ProcessedOntology processedOntology0 = ProcessedOntology.builder()
            .id(0)
            .ontologyId("ontology_0")
            .uri("https://something0.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_20"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_20"))
            .build();

        when(preProcessingOntologyService.preProcess(any(Optional.class), anyString()))
            .thenReturn(processedOntology0);

        mockMvc.perform(
                get("/api/ontology/similarity/pairwise/external")
                    .param("url", "https://raw.githubusercontent.com/LiUSemWeb/Materials-Design-Ontology/master/mdo-full.owl")
                    .contentType(MediaType.APPLICATION_JSON)
            )
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
                    CharacteristicsInfo.of(List.of("prop1", "prop2"), 10, 2),
                    CharacteristicsType.IMPORT.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("import1", "import2"), 10, 2),
                    CharacteristicsType.CLASS.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("class1", "class2"), 10, 2),
                    CharacteristicsType.NAMESPACE.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("namespace1", "namespace2"), 10, 2)
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
                    CharacteristicsInfo.of(List.of("prop1", "prop2"), 10, 2),
                    CharacteristicsType.IMPORT.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("import1", "import2"), 10, 2),
                    CharacteristicsType.CLASS.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("class1", "class2"), 10, 2),
                    CharacteristicsType.NAMESPACE.name().toLowerCase(),
                    CharacteristicsInfo.of(List.of("namespace1", "namespace2"), 10, 2)
                )
            )
            .build();

        return new PageImpl<>(List.of(pairwiseSimilarity0, pairwiseSimilarity1), PAGEABLE, 1);
    }
}
