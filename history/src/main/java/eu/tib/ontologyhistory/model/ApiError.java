package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @JsonView({Views.Update.class, Views.Full.class})
    private String ontologyId;

    @JsonView({Views.Update.class, Views.Full.class})
    private String status;

    @JsonView({Views.Update.class, Views.Full.class})
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    @JsonView({Views.Update.class, Views.Full.class})
    private String message;

    @JsonView({Views.Update.class, Views.Full.class})
    private String debugMessage;

    @JsonView({Views.Update.class, Views.Full.class})
    private String leftIriFile;

    @JsonView({Views.Update.class, Views.Full.class})
    private String rightIriFile;

}
