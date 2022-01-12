package eu.tib.ts.controller;

import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.service.RatioService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RatioController.class)
class RatioControllerTest {

    public static final double RATIO_RESULT = 0.5;

    @MockBean
    RatioService ratioService;

    @Autowired
    MockMvc mockMvc;

    @SneakyThrows
    @Test
    void testGetRatio() {
        when(ratioService.getRatio(anyList(), any(CharacteristicsType.class)))
            .thenReturn(RATIO_RESULT);

        String body = "[{\"ontologyId\" : \"dicl\",\"uri\" : \"\"},\n" +
            "{\"ontologyId\" : \"dicob\",\"uri\" : \"\"}]";

        mockMvc.perform(get("/api/ontology/ratio/property")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.value", is(RATIO_RESULT)))
            .andReturn();
    }
}
