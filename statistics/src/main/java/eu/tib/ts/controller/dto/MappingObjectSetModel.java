package eu.tib.ts.controller.dto;

import lombok.*;

/**
 * Information about mappings between a pair of ontologies that are stored in MongoDB / JSON
 */
@Getter
@Setter
public class MappingObjectSetModel {


    String  sourceIRI;

    String sourceLabel;

    int mappingDirection;

    String targetIRI;

    String targetLabel;

    int typeOfMapping;

    double structuralConfidenceMapping;

    double confidence;

}
