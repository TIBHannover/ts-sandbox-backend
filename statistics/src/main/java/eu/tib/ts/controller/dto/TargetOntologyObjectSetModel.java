package eu.tib.ts.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Author: Nenad Krdzavac
 * Email: Nenad.Krdzavac@tib.eu
 *
 * This class stores information about target ontology,
 * number of mappings, number of conflictive mappings, as well as
 * list of mappings, and list of conflictive mappings.
 */
@Getter
@Setter
public class TargetOntologyObjectSetModel {

    long id;

    OntologyDto targetOntology;

    StatisticsDto statistics;

    String mappingException;

    Set<MappingObjectSetModel> mappingList;

    Set<MappingObjectSetModel> conflictiveMappingList;

}