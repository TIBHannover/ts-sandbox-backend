package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
public class ApiError {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private String ontologyId;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    private String message;

    private String debugMessage;

    private String leftIriFile;

    private String rightIriFile;

}
