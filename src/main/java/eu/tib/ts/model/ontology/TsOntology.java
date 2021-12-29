package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TsOntology {
    String ontologyId;
    String loaded;
    String updated;
    String status;
    String message;
    Object version;
    String fileHash;
    int loadAttempts;
    int numberOfTerms;
    int numberOfProperties;
    int numberOfIndividuals;
    Config config;
}
