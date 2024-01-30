package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.OriginOfValueAddedModel;
import eu.tib.tiva.service.OriginOfValueAddedService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class OriginOfValueAddedServiceImpl implements OriginOfValueAddedService {

    @Override
    public <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddedInFinalDomain(String sparqlEndpoint, Pageable pageable) {
        return null;
    }

    @Override
    public <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddedAndFinalDestination(String sparqlEndpoint, Pageable pageable) {
        return null;
    }

    @Override
    public <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddeInGrossImports(String sparqlEndpoint, Pageable pageable) {
        return null;
    }

    @Override
    public <T extends OriginOfValueAddedModel> Page<OriginOfValueAddedModel> getOriginOfValueAddeInGrossExports(String sparqlEndpoint, Pageable pageable) {
        return null;
    }
}
