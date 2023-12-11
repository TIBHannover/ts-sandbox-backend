package eu.tib.ts.listener;

import eu.tib.ts.service.PreProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "preprocessing", name = "run")
public class PreProcessingRunner implements ApplicationListener<ApplicationReadyEvent> {
    private final PreProcessingService preProcessingService;

    @Autowired
    public PreProcessingRunner(PreProcessingService preProcessingService) {
        log.info("starting PreProcessingRunner");
        log.error("Titled : PreProcessingRunner "  );

        this.preProcessingService = preProcessingService;
//        preProcessingService.doPreProcessing();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        log.error("Titled : Event Executed "  );
        //        preProcessingService.doPreProcessing();

    }
}