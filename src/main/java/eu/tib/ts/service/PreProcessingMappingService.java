package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedMapping;

public interface PreProcessingMappingService {

    ProcessedMapping preProcess(int typeOfMapping, String sourceIRI, String targetIRI);
}
