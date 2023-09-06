package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedMapping;

import java.util.Set;

/**
 * Author: Nenad Krdzavac
 * Email: Nenad.Krdzavac@tib.eu
 *
 * This class stores information about target ontology,
 * number of mappings, number of conflictive mappings, as well as
 * list of mappings, and list of conflictive mappings
 */
public class TargetOntologyDto {

    long id;
    String ontologyId;
    String uri;
    String title;
    int numberOfMappings;
    int numberOfConflictiveMappings;

    Set<MappingObjectSetModel> mappingList;
    Set<MappingObjectSetModel> conflictiveMappingsList;



}
