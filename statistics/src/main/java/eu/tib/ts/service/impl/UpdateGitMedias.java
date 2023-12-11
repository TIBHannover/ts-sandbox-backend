package eu.tib.ts.service.impl;

import eu.tib.ts.service.GitPreProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateGitMedias {
    @Autowired
    private GitPreProcessingService preProcessingService;

    @Scheduled(fixedDelay = 86400000)
    public void doSomething() {

        System.out.println("I am execting after two minute");

        preProcessingService.doGitPreProcessing();

    }

}
