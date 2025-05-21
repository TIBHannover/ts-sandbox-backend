package eu.tib.ontologyhistory.service.scheduler;

import eu.tib.ontologyhistory.service.OndetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
@Slf4j
public class OntologyScheduler {

    private OndetService ondetService;

    private static final String DATASET = "test";

    private final AtomicReference<CompletableFuture<Void>> running = new AtomicReference<>();

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 7,12,19 * * 1-5")
    public void scheduledOntologyChecks() {
        if (running.get() != null && !running.get().isDone()) {
            log.error("Scheduled tasks are already running. New execution skipped.");
            return;
        }

        val future = CompletableFuture.runAsync(() -> {
            scheduledNewTsOntologies();
            scheduledCheckNewOntologyVersions();

        });

        running.set(future);
    }

    private void scheduledNewTsOntologies() {
        log.error("Ondet check started");
        val tsOntologies = ondetService.getTSOntologies();
        val filteresTsOntologies = ondetService.filterUnsupportedOntologyTypes(tsOntologies);
        val existingOntologes = ondetService.findAll();
        filteresTsOntologies.removeAll(existingOntologes);
        if (!filteresTsOntologies.isEmpty()) {
            ondetService.createBatchAsync(filteresTsOntologies, DATASET);
        }
    }

    private void scheduledCheckNewOntologyVersions() {
        log.error("Scheduled check old ontology versions");
        val ontologies = ondetService.findAll();
        for (val ontology : ontologies) {
            val lastTsVersion = ondetService.getVersion(ontology);
            if (lastTsVersion != null) {
                val lastRemoteVersion = ondetService.getCommits(ontology).get(0);
                if (lastRemoteVersion.getDatetime().isAfter(lastTsVersion.datetime())) {
                    ondetService.updateByUrlAsync(ontology, lastTsVersion.datetime(), DATASET);
                }
            }
        }
    }

}
