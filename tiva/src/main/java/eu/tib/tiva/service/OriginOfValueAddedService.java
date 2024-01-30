package eu.tib.tiva.service;

import eu.tib.tiva.model.OriginOfValueAdded;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OriginOfValueAddedService {

    <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddedInFinalDomain(String sparqlEndpoint,
                                                                                               Pageable pageable);

    <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddedAndFinalDestination(String sparqlEndpoint,
                                                                                                     Pageable pageable);

    <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddeInGrossImports(String sparqlEndpoint,
                                                                                               Pageable pageable);

    <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddeInGrossExports(String sparqlEndpoint,
                                                                                               Pageable pageable);
}