package eu.tib.ts.service.impl;

import eu.tib.ts.service.GitPreProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UpdateGitMedias {
    @Autowired
    private GitPreProcessingService preProcessingService;

    @Scheduled(initialDelay = 7 * 86400000, fixedDelay = 7 * 86400000)
    public void doSomething() {

        System.out.println("executing once in a week");

        try{
            TimeUnit.SECONDS.sleep(25);
            preProcessingService.doGitPreProcessing();
        }
        catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
