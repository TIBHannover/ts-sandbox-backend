package eu.tib.ts.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SourceOntologyObjectSetModel {

//    OntologyDto sourceOntology;
    long id;
    String ontologyId;
    String uri;
    String title;
    /*
    added 01.03.2023.
    ontology collection
     */
    Set<String> collection;

}
