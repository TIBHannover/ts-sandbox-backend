package eu.tib.ontologyhistory.service.scheduler;

import eu.tib.ontologyhistory.service.OndetService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
@Slf4j
public class OntologyScheduler {

    private OndetService ondetService;

    private static final String DATASET = "test";

    private final AtomicReference<CompletableFuture<Void>> running = new AtomicReference<>();

    // TODO Find why async create and update methods create wrong results (often empty for all diff types)
//    @EventListener(ApplicationReadyEvent.class)
//    @Scheduled(cron = "0 0 7,12,19 * * 1-7")
    public void scheduledOntologyChecksAsync() {
        if (running.get() != null && !running.get().isDone()) {
            log.error("Scheduled tasks are already running. New execution skipped.");
            return;
        }

        val future = CompletableFuture.runAsync(() -> {
            scheduledNewTsOntologies();
            scheduledCheckNewOntologyVersions(true);

        });

        running.set(future);
    }

    @EventListener(ApplicationReadyEvent.class)
//    @Scheduled(cron = "0 0 7,12,19 * * 1-7")
    public void scheduledOntologyChecks() {
        scheduledNewTsOntologies();
        scheduledCheckNewOntologyVersions(false);
    }

    private List<URI> getNewOntologies() {
        val tsOntologies = ondetService.getTSOntologies();
        val filteredTsOntologies = ondetService.filterUnsupportedOntologyTypes(tsOntologies);
        val existingOntologes = ondetService.findAll();
        filteredTsOntologies.removeAll(existingOntologes);
        return filteredTsOntologies;
    }

    private void scheduledNewTsOntologiesAsync() {
        log.error("Ondet check started with async");
        val filteredTsOntologies = getNewOntologies();
        if (!filteredTsOntologies.isEmpty()) {
            ondetService.createBatchAsync(filteredTsOntologies, DATASET);
        }
    }

    private void scheduledNewTsOntologies() {
        log.error("Ondet check started");
        val filteredTsOntologies = getNewOntologies();
        if (!filteredTsOntologies.isEmpty()) {
            for (val ontology : filteredTsOntologies) {
                ondetService.create(ontology, DATASET);
            }
        }
    }

    private void scheduledCheckNewOntologyVersions(boolean updatedAsync) {
        log.error("Scheduled check old ontology versions");
        val ontologies = ondetService.findAll();
        for (val ontology : ontologies) {
            val lastTsVersion = ondetService.getVersion(ontology);
            if (lastTsVersion != null) {
                val lastRemoteVersion = ondetService.getCommits(ontology).get(0);
                if (lastRemoteVersion.getDatetime().isAfter(lastTsVersion.datetime())) {
                    if (updatedAsync) {
                        ondetService.updateAsync(ontology, lastTsVersion.datetime(), DATASET);
                    } else {
                        ondetService.update(ontology, lastTsVersion.datetime(), DATASET);
                    }
                }
            }
        }
    }

}
