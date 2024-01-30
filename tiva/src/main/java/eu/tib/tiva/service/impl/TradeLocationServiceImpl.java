package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.TradeLocationCode;
import eu.tib.tiva.model.CountryCodesModel;
import eu.tib.tiva.service.TradeLocationService;
import eu.tib.tiva.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@EnableAutoConfiguration
public class TradeLocationServiceImpl implements TradeLocationService {

    @Override
    public <T extends CountryCodesModel> Page<TradeLocationCode> getCountryCodeList(String sparqlEndpoint,
                                                                                    String type,
                                                                                    Pageable pageable) {
        log.info("query trade location codes started");

        Repository repo = new SPARQLRepository(sparqlEndpoint);

        repo.initialize();

        List<String> tradeCodeStringList = new ArrayList<>();
        List<TradeLocationCode> tradeLocationCodeList = new ArrayList<>();

        try (RepositoryConnection conn = repo.getConnection()) {

            TupleQuery tupleQuery = conn.prepareTupleQuery(getTradeLocationCodes(type));

            try (TupleQueryResult result = tupleQuery.evaluate()) {

                Set<BindingSet> resultList = QueryResults.asSet(result);

                for (BindingSet bindingSet : resultList) {

                Value tradeLocationCode  = bindingSet.getValue("tradeLocationCode");
                tradeCodeStringList.add(tradeLocationCode.stringValue());

                }

            }catch (Exception e ){

                e.printStackTrace();

            }

        }catch (Exception e){

        e.printStackTrace();

        }

        TradeLocationCode tradeLocationCode = processTradeLocationCode(UUID.randomUUID().toString(), tradeCodeStringList);
        tradeLocationCodeList.add(tradeLocationCode);
        repo.shutDown();

        return PageUtils.toPage(tradeLocationCodeList, pageable);
    }

    private TradeLocationCode processTradeLocationCode(String id,
                                                       List<String> tradeLocationCodeList) {
        return TradeLocationCode.builder()
                .id(id)
                .tradeLocationCodeList(tradeLocationCodeList)
                .build();
    }
    public static String getTradeLocationCodes(String type) {

        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               "SELECT DISTINCT ?tradeLocation  " +
               "WHERE { " +
               "?s  <https://schema.coypu.org/vtf#hasTradeLocation> ?tradeLocationCode . " +
               "?tradeLocationCode rdf:type <https://schema.coypu.org/global#"+type+" . " +
               "} LIMIT 1000 ";
    }
}