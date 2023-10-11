package eu.tib.ontologyhistory.model;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;
    private String leftIriFile;
    private String rightIriFile;

}
