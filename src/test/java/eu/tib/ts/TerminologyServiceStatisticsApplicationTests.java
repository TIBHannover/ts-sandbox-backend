package eu.tib.ts;

import eu.tib.ts.service.SimilarityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TerminologyServiceStatisticsApplicationTests {
    @Autowired
    private SimilarityService similarityService;

    @Test
    void contextLoads() {
        assertNotNull(similarityService);
    }
}
