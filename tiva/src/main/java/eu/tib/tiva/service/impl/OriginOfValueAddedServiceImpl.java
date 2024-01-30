package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.OriginOfValueAdded;
import eu.tib.tiva.service.OriginOfValueAddedService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class OriginOfValueAddedServiceImpl implements OriginOfValueAddedService {

    @Override
    public <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddedInFinalDomain(String sparqlEndpoint, Pageable pageable) {
        return null;
    }

    @Override
    public <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddedAndFinalDestination(String sparqlEndpoint, Pageable pageable) {
        return null;
    }

    @Override
    public <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddeInGrossImports(String sparqlEndpoint, Pageable pageable) {
        return null;
    }

    @Override
    public <T extends OriginOfValueAdded> Page<OriginOfValueAdded> getOriginOfValueAddeInGrossExports(String sparqlEndpoint, Pageable pageable) {
        return null;
    }
}
