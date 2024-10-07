package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
public class GitDiff {

    @Id
    private String id;

    private String url;

    private String sha;

    private String parentSha;

    private String diff;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant datetime;

}
