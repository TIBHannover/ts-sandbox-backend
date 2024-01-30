package eu.tib.tiva.service;

import eu.tib.tiva.model.OriginOfValueAddedModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OriginOfValueAddedService {

    <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddedInFinalDomain(String sparqlEndpoint,
                                                                             Pageable pageable);

    <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddedAndFinalDestination(String sparqlEndpoint,
                                                                                                         Pageable pageable);

    <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddeInGrossImports(String sparqlEndpoint,
                                                                                                         Pageable pageable);

    <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddeInGrossExports(String sparqlEndpoint,
                                                                                                         Pageable pageable);

}