package eu.tib.ts;

import eu.tib.ts.controller.SimilarityController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TerminologyServiceStatisticsApplicationTests {
    @Autowired
    private SimilarityController similarityController;

    @Test
    void contextLoads() {
        assertNotNull(similarityController);
    }
}
