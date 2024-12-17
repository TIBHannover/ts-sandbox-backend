package eu.tib.ts.assessments.listener;

import eu.tib.ts.assessments.services.GitPreProcessingService;
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
//    private final PreProcessingService preProcessingService;
    private final GitPreProcessingService gitPreProcessingService;

    @Autowired
    public PreProcessingRunner(GitPreProcessingService gitPreProcessingService) {
        log.info("starting PreProcessingRunner");
        log.error("Titled : PreProcessingRunner "  );
        System.out.println("getting proccess");

//        this.preProcessingService = preProcessingService;
        this.gitPreProcessingService = gitPreProcessingService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        log.error("Titled : Event Executed "  );
        gitPreProcessingService.performGitPreProcessing();

    }
}