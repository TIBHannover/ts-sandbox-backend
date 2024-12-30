package eu.tib.ts.assessments.services.impl;

import eu.tib.ts.assessments.services.GitPreProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UpdateGitMedias {
    @Autowired
    private GitPreProcessingService preProcessingService;

    @Scheduled(fixedDelay = 7)  //    @Scheduled(fixedDelay = 7 * 86400000)
    public void doSomething() {

        System.out.println("executing once in a week");

        try{
            TimeUnit.SECONDS.sleep(1);
            preProcessingService.performGitPreProcessing();
        }
        catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}