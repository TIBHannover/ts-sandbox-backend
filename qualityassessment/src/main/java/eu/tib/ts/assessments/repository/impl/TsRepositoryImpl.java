package eu.tib.ts.assessments.repository.impl;

import eu.tib.ts.assessments.model.tags.ontology.TsOntology;
import eu.tib.ts.assessments.repository.TsRepository;
import eu.tib.ts.assessments.repository.exception.TsRepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class TsRepositoryImpl implements TsRepository {
    private static final String QUERY_PARAM_SIZE = "size";

    @Value("${ts.base.uri}")
    private String tsBaseUri;

    @Value("${ts.ontologies.list.size}")
    private int ontologiesListSize;

    private final RestTemplate restTemplate;

    @Autowired
    public TsRepositoryImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<TsOntology> getOntologies() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
            .fromHttpUrl(tsBaseUri);

        uriComponentsBuilder.queryParam(QUERY_PARAM_SIZE, ontologiesListSize);

        log.error("Titled before parsing"  );
        System.out.println("Titled before parsing");

        log.info("starting getOntologies");
        ResponseEntity<PagedModel<TsOntology>> responseEntity =
            restTemplate
                .exchange(
                    uriComponentsBuilder.build().toUriString(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<PagedModel<TsOntology>>() {
                    }
                );

        PagedModel<TsOntology> body = responseEntity.getBody();
        log.error("Titled after parsing"  );

        if (Objects.isNull(body)) {
            log.error("can not parsing"  );

        throw new TsRepositoryException("Could not get response");

        }

        Collection<TsOntology> content = body.getContent();

        return new ArrayList<>(content);
    }

}