package eu.tib.ts.repository.impl;

import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
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

@Repository
public class TsRepositoryImpl implements TsRepository {
    private static final String ONTOLOGIES_LIST_PATH = "/ts4tib/api/ontologies";
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
            .fromHttpUrl(tsBaseUri + ONTOLOGIES_LIST_PATH);

        uriComponentsBuilder.queryParam(QUERY_PARAM_SIZE, ontologiesListSize);

        ResponseEntity<PagedModel<TsOntology>> responseEntity =
            restTemplate
                .exchange(
                    uriComponentsBuilder.build().toUriString(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<>() {
                    }
                );

        PagedModel<TsOntology> body = responseEntity.getBody();

        if (Objects.isNull(body)) {
            throw new RuntimeException("Could not get response");
        }

        Collection<TsOntology> content = body.getContent();

        return new ArrayList<>(content);
    }

}
