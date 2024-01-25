package eu.tib.tiva.service.impl;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import eu.tib.tiva.model.CountryCode;
import eu.tib.tiva.model.CountryCodesModel;
import eu.tib.tiva.service.CountryCodeService;
import eu.tib.tiva.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@EnableAutoConfiguration
public class CountryCodeServiceImpl implements CountryCodeService {

    /**
     * S
     * Tiva graph name used in this query string
     * can be also passes as a parameter in the rest call
     */
    private String queryCountryCode =
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "SELECT DISTINCT ?countryCode  WHERE { " +
            "GRAPH <https://data.coypu.org/trade/tiva/> " +
            "{ " +
            "?countryCode rdf:type <https://schema.coypu.org/global#Country>  . " +
            "} " +
            "} LIMIT 200 ";

    @Override
    public <T extends CountryCodesModel> Page<CountryCode> getCountryCodeList(String sparqlEndpoint,
                                                                              String userName,
                                                                              String password,
                                                                              Pageable pageable) {
        /**
         * Simple authentication to SkyNet server.
         * Users should add user name and password in rest call parameters.
         */
        org.apache.jena.atlas.web.auth.HttpAuthenticator authenticator =
                new PreemptiveBasicAuthenticator(new SimpleAuthenticator(userName, password.toCharArray()), true);

        List<String> countryCodeStringList = new ArrayList<String>();

        List<CountryCode> countryCodeList = new ArrayList<CountryCode>();

        try(QueryExecution queryExecution = QueryExecutionFactory.sparqlService(sparqlEndpoint, queryCountryCode, authenticator)){

        ResultSet resultSet = queryExecution.execSelect();

            while(resultSet.hasNext()){

                QuerySolution querySolution = resultSet.next() ;

                RDFNode countryCodeRDFNode = querySolution.get("countryCode") ;

                countryCodeStringList.add(countryCodeRDFNode.toString());
            }

        }catch(Exception e) {

            e.printStackTrace();
        };

        CountryCode countryCode = processCountryCode(UUID.randomUUID().toString(), countryCodeStringList);

        countryCodeList.add(countryCode);

        return PageUtils.toPage(countryCodeList, pageable);
    }

    private CountryCode processCountryCode(String id,
                                           List<String> countryCodeList) {
        return CountryCode.builder()
                .id(id)
                .countryCodeList(countryCodeList)
                .build();
    }


}