package eu.tib.ts.controller.dto;

import lombok.*;

@Getter
@Setter
public class MappingObjectSetModel {


    String  sourceIRI;
    int mappingDirection;
    String targetIRI;
    int typeOfMapping;
    double structuralConfidenceMapping;
    double confidence;

}
