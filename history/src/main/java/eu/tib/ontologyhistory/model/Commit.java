package eu.tib.ontologyhistory.model;

import java.time.Instant;

public interface Commit {
    Instant getDatetime();
}
