package eu.tib.ontologyhistory.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
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
