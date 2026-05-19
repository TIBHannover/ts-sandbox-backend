package eu.tib.ts.repository.impl;

import eu.tib.ts.configuration.TsProperties;
import eu.tib.ts.model.ontology.TibOntologyApiV2Response;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.repository.exception.TsRepositoryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
public class TsRepositoryImpl implements TsRepository {
    private static final String QUERY_PARAM_PAGE = "page";
    private static final String QUERY_PARAM_SIZE = "size";
    private static final String QUERY_PARAM_EXACT_MATCH = "exactMatch";
    private static final String QUERY_PARAM_INCLUDE_OBSOLETE = "includeObsoleteEntities";
    private static final String QUERY_PARAM_SCHEMA = "schema";
    private static final String QUERY_PARAM_EXCLUSIVE = "exclusive";
    private static final String QUERY_PARAM_OPTION = "option";

    private final RestTemplate restTemplate;
    private final TsProperties tsProperties;

    @Autowired
    public TsRepositoryImpl(RestTemplate restTemplate, TsProperties tsProperties) {
        this.restTemplate = restTemplate;
        this.tsProperties = tsProperties;
    }

    @Override
    public List<TsOntology> getOntologies() {
        List<TsOntology> allOntologies = new ArrayList<>();
        int currentPage = 0;
        boolean hasMore = true;

        log.info("Starting to fetch ontologies from TIB API v2: {} (limit: {})", tsProperties.getBaseUri(), tsProperties.getOntologiesListSize());
        if (!CollectionUtils.isEmpty(tsProperties.getClassifications())) {
            log.info("Classifications filter: {}", tsProperties.getClassifications());
        } else {
            log.warn("No classifications configured - will fetch all ontologies");
        }

        while (hasMore) {
            TibOntologyApiV2Response response = fetchOntologiesPage(currentPage);

            if (response != null && response.getElements() != null) {
                List<TsOntology> pageOntologies = response.getElements().stream()
                        .map(element -> element.toTsOntology())
                        .collect(Collectors.toList());

                allOntologies.addAll(pageOntologies);
                log.info("Fetched page {} with {} ontologies. Total so far: {}", 
                        currentPage, pageOntologies.size(), allOntologies.size());

                // Stop if we've reached the limit
                if (allOntologies.size() >= tsProperties.getOntologiesListSize()) {
                    allOntologies = allOntologies.stream()
                            .limit(tsProperties.getOntologiesListSize())
                            .collect(Collectors.toList());
                    hasMore = false;
                    log.info("Reached ontologies limit of {}", tsProperties.getOntologiesListSize());
                } else if (response.getPage() < response.getTotalPages() - 1) {
                    currentPage++;
                } else {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
        }

        log.info("Successfully fetched {} ontologies from TIB API v2", allOntologies.size());
        return allOntologies;
    }

    private TibOntologyApiV2Response fetchOntologiesPage(int page) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(tsProperties.getBaseUri())
                    .queryParam(QUERY_PARAM_PAGE, page)
                    .queryParam(QUERY_PARAM_SIZE, tsProperties.getOntologiesListSize())
                    .queryParam(QUERY_PARAM_EXACT_MATCH, false)
                    .queryParam(QUERY_PARAM_INCLUDE_OBSOLETE, false)
                    .queryParam(QUERY_PARAM_SCHEMA, "collection")
                    .queryParam(QUERY_PARAM_EXCLUSIVE, false)
                    .queryParam(QUERY_PARAM_OPTION, "COMPOSITE");

            // Add classification filters if configured
            if (!CollectionUtils.isEmpty(tsProperties.getClassifications())) {
                for (String classification : tsProperties.getClassifications()) {
                    uriBuilder.queryParam("classification", classification);
                }
                log.info("Filtering ontologies by classifications: {}", tsProperties.getClassifications());
            }

            String url = uriBuilder.build().toUriString();
            log.info("Requesting ontologies from: {}", url);

            ResponseEntity<TibOntologyApiV2Response> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    TibOntologyApiV2Response.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                log.info("Successfully fetched page {} with {} elements", page, 
                        responseEntity.getBody().getNumElements());
                return responseEntity.getBody();
            } else {
                log.error("Unexpected response status: {}", responseEntity.getStatusCode());
                throw new TsRepositoryException("Could not fetch ontologies from TIB API");
            }
        } catch (Exception e) {
            log.error("Error fetching ontologies from TIB API", e);
            throw new TsRepositoryException("Could not get response from TIB API: " + e.getMessage());
        }
    }
}
