package eu.tib.ontologyhistory.controller;

import eu.tib.ontologyhistory.dto.OntologyGitDiffRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Setter
@Getter
public class RequestDetails {
    private OntologyGitDiffRequest requestBody;
}
