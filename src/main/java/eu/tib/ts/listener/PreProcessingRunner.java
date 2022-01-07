package eu.tib.ts.listener;

import eu.tib.ts.service.PreProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "preprocessing", name = "run")
public class PreProcessingRunner implements ApplicationListener<ApplicationReadyEvent> {
    private final PreProcessingService preProcessingService;

    @Autowired
    public PreProcessingRunner(PreProcessingService preProcessingService) {
        this.preProcessingService = preProcessingService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        preProcessingService.doPreProcessing();
    }
}
