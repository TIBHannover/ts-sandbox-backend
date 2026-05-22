package eu.tib.ts.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SourceOntologyObjectSetModel {

    long id;
    String ontologyId;
    String uri;
    String title;
    /*
     * ontology collection
     */
    Set<String> collection;
    String description;
    StatisticsDto statistics;

}
