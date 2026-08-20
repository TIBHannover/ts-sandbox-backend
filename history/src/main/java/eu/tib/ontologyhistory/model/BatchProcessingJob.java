package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@Jacksonized
public class BatchProcessingJob {

    @Id
    private String id;

    private BatchJobStatus status;

    private int total;

    private int processed;

    private int added;

    private int notAdded;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant finishedAt;

    private String message;

    private List<BatchOntologyResult> results;
}
