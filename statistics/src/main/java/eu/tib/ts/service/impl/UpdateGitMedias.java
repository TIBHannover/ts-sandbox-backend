package eu.tib.ts.service.impl;

import eu.tib.ts.service.GitPreProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UpdateGitMedias {
    @Autowired
    private GitPreProcessingService preProcessingService;

    @Scheduled(initialDelay = 7 * 86400000, fixedDelay = 7 * 86400000)
    public void doSomething() {

        System.out.println("I am execting after one week");


        // Create a Runnable object
        Runnable myRunnable = new MyRunnable();

        // Create a thread and associate it with the Runnable
        Thread thread = new Thread(myRunnable);

        // Start the thread
        thread.start();


    }

    class MyRunnable implements Runnable {
        public void run() {
            preProcessingService.doGitPreProcessing();
        }

    }

}
