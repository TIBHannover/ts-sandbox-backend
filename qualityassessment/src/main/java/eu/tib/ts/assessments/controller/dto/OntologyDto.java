package eu.tib.ts.assessments.controller.dto;

import eu.tib.ts.assessments.model.tags.ontology.ProcessedOntology;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Builder
@Value
public class OntologyDto {

    long id;
    String ontologyId;
    String uri;
    String title;
    Set<String> collection;


}