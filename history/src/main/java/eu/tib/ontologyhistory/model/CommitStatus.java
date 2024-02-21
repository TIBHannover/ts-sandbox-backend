package eu.tib.ontologyhistory.model;

import com.fasterxml.jackson.annotation.JsonView;
import eu.tib.ontologyhistory.view.Views;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommitStatus {
    @JsonView({Views.Short.class, Views.Full.class})
    private String status;
    @JsonView({Views.Short.class, Views.Full.class})
    private String commitsBehind;
    @JsonView({Views.Short.class, Views.Full.class})
    private String branch;
}
