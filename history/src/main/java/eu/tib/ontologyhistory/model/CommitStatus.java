package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.tib.ontologyhistory.view.Views;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommitStatus {
    @JsonView({Views.Short.class, Views.Full.class})
    private String status;
    @JsonView({Views.Short.class, Views.Full.class})
    private String commitsBehind;
    @JsonView({Views.Short.class, Views.Full.class})
    private String branch;
}
