package eu.tib.ts.service.impl;

import eu.tib.ts.service.PreProcessingService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "dev")
@SpringBootTest
@Disabled
public class PreProcessingIntegrationTest {
    @Autowired
    private PreProcessingService preProcessingService;

    @Test
    void runPreProcessing() {
        preProcessingService.doPreProcessing();
    }
}
