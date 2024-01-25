package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.CountryCode;
import eu.tib.tiva.model.CountryCodesModel;
import eu.tib.tiva.service.CountryCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableAutoConfiguration
public class CountryCodeServiceImpl implements CountryCodeService {

    /**
     * Graph name can be also passes as a parameter in the rest call
     */
    private String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "SELECT DISTINCT ?s  WHERE { " +
            "GRAPH <https://data.coypu.org/trade/tiva/> " +
            "{ " +
            "?s rdf:type <https://schema.coypu.org/global#Country>  . " +
            "} " +
            "} LIMIT 200 ";

    @Override
    public <T extends CountryCodesModel> Page<CountryCode> getCountryCodeList(String sparqlEndpoint,
                                                                              String userName,
                                                                              String password,
                                                                              Pageable pageable) {

        return null;
    }

}