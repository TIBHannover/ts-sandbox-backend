package eu.tib.tiva.listener;

import eu.tib.tiva.service.PreProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix="preprocessing",name="run")
public class PreProcessingRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final PreProcessingService preProcessingService;

    PreProcessingRunner(PreProcessingService preProcessingService){

        log.info("start pre processing runner");

        this.preProcessingService = preProcessingService;

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        log.info("start pre processing:");
        log.info("-----> running queries");
        log.info("-----> storing query results into MongoDB");

        preProcessingService.doPreProcessing();
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
