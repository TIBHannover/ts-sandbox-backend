package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.ProcessedOntology;
import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;
import java.util.Set;

@Builder
@Value
public class OntologyDto {

    long id;
    String ontologyId;
    String uri;
    String title;
    /*
    added 01.03.2023.
    ontology collection
     */
    Set<String> collection;

    public static OntologyDto of(ProcessedOntology processedOntology) {

        return OntologyDto.builder()
                .id(processedOntology.getId())
                .ontologyId(processedOntology.getOntologyId())
                .uri(processedOntology.getUri())
                .title(processedOntology.getTitle())
                .build();

    }

    public static OntologyDto mappingOf(ProcessedOntology processedOntology) {

        return OntologyDto.builder()
                .id(processedOntology.getId())
                .ontologyId(processedOntology.getOntologyId())
                .uri(processedOntology.getUri())
                .title(processedOntology.getTitle())
                .collection(processedOntology.getCollection())
                .build();

    }

}