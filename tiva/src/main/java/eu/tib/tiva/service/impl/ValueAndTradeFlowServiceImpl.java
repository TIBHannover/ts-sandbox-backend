package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.OriginOfValueAddedInFinalDemand;
import eu.tib.tiva.model.ValueAndTradeFlowCode;
import eu.tib.tiva.model.ValueAndTradeFlowCodesModel;
import eu.tib.tiva.service.ValueAndTradeFlowService;
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
public class ValueAndTradeFlowServiceImpl implements ValueAndTradeFlowService {

    @Override
    public <T extends ValueAndTradeFlowCodesModel> Page<ValueAndTradeFlowCode> getValueAndTradeFlowCodeList(String sparqlEndpoint,
                                                                                                            String type,
                                                                                                            Pageable pageable) {
        log.info("query value and trade flow codes started: ");

        Repository repo = new SPARQLRepository(sparqlEndpoint);

        repo.initialize();

        List<String> tradeCodeStringList = new ArrayList<>();
        List<ValueAndTradeFlowCode> valueAndTradeFlowCodeList = new ArrayList<>();

        try (RepositoryConnection conn = repo.getConnection()) {

            /**
             * InetrnationalOrganization should be written as InternationalOrganization in Python code
             */
            if(type.equals("Country") || type.equals("InetrnationalOrganization")) {

            tradeCodeStringList= getCodeStringList(conn,getTradeLocationCodes(type));

            } else if(type.equals("IndustryCode")){

            tradeCodeStringList= getCodeStringList(conn,getIndustryCodes());

            }

        }catch (Exception e){

        e.printStackTrace();

        }

        ValueAndTradeFlowCode valueAndTradeFlowCode = processValueAndTradeFlowCode(UUID.randomUUID().toString(), tradeCodeStringList);
        valueAndTradeFlowCodeList.add(valueAndTradeFlowCode);
        repo.shutDown();

        return PageUtils.toPage(valueAndTradeFlowCodeList, pageable);
    }

    @Override
    public <T extends ValueAndTradeFlowCodesModel> Page<OriginOfValueAddedInFinalDemand> getOriginOfValueAddedInFinalDemandList(String sparqlEndpoint,
                                                                                                                                String location,
                                                                                                                                String industry,
                                                                                                                                Pageable pageable) {
        log.info("query origin of value aded in final demand started: ");

        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.initialize();

        List<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandList = new ArrayList<>();



        return null;

    }

    private List<OriginOfValueAddedInFinalDemand> getOriginOfValueAddedInFinalDemand(RepositoryConnection conn,
                                                                                     String queryString){

        List<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandList = new ArrayList<>();

        TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);

        try (TupleQueryResult result = tupleQuery.evaluate()) {

            Set<BindingSet> resultList = QueryResults.asSet(result);

            for (BindingSet bindingSet : resultList) {

                //?fdTradeLocation ?fdIndustryCode ?vao_fd_value  ?vao_fd_year
                Value  fdTradeLocation = bindingSet.getValue("fdTradeLocation");
                Value  fdIndustryCode = bindingSet.getValue("fdIndustryCode");
                Value  vao_fd_value = bindingSet.getValue("vao_fd_value");
                Value  vao_fd_year = bindingSet.getValue("vao_fd_year");

                OriginOfValueAddedInFinalDemand originOfValueAddedInFinalDemand = processOriginOfValueAddedInFinalDemand(
                        UUID.randomUUID().toString(),
                        fdTradeLocation.stringValue(),
                        fdIndustryCode.stringValue(),
                        vao_fd_value.stringValue(),
                        vao_fd_year.stringValue()
                );

                originOfValueAddedInFinalDemandList.add(originOfValueAddedInFinalDemand);

            }

            return originOfValueAddedInFinalDemandList;

        } catch (Exception e) {

            e.printStackTrace();

        }

        return originOfValueAddedInFinalDemandList;

    }

    private List<String> getCodeStringList(RepositoryConnection conn , String queryString){

        List<String> tradeCodeStringListTemp = new ArrayList<>();

        TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);

        try (TupleQueryResult result = tupleQuery.evaluate()) {

            Set<BindingSet> resultList = QueryResults.asSet(result);

            for (BindingSet bindingSet : resultList) {

                Value code = bindingSet.getValue("code");

                tradeCodeStringListTemp.add(code.stringValue());

            }

        return tradeCodeStringListTemp;

        } catch (Exception e) {

            e.printStackTrace();

        }

    return tradeCodeStringListTemp;

    }

    private OriginOfValueAddedInFinalDemand processOriginOfValueAddedInFinalDemand(String id,
                                                                                   String countryCode,
                                                                                   String industryCode,
                                                                                   String value,
                                                                                   String year){
        return OriginOfValueAddedInFinalDemand.builder()
                .id(id)
                .countryCode(countryCode)
                .industryCode(industryCode)
                .value(value)
                .year(year)
                .build();

    }

    private ValueAndTradeFlowCode processValueAndTradeFlowCode(String id,
                                                               List<String> tradeCodeList) {
        return ValueAndTradeFlowCode.builder()
                .id(id)
                .valueAndTradeFlowCodeList(tradeCodeList)
                .build();
    }

    public static String getTradeLocationCodes(String type) {

        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               "SELECT DISTINCT ?code  " +
               "WHERE { " +
               "?s  <https://schema.coypu.org/vtf#hasTradeLocation> ?code . " +
               "?code rdf:type <https://schema.coypu.org/global#"+type+"> . " +
               "} LIMIT 5000 ";
    }

    public static String getIndustryCodes(){

        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               "SELECT DISTINCT ?code " +
               "WHERE { " +
               "?s <https://schema.coypu.org/vtf#hasIndustryCode> ?code . " +
               "} LIMIT 5000 ";
    }

    public static String getValueAddedOriginInFinalDemand(String location, String industryCode){

        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               "SELECT DISTINCT ?fdTradeLocation ?fdIndustryCode ?vao_fd_value  ?vao_fd_year " +
               "WHERE { " +
               "?vao_fd rdf:type <https://schema.coypu.org/vtf#FdVaBsci> . " +
               "?vao_fd <https://schema.coypu.org/global#hasValue> ?vao_fd_value . " +
               "?vao_fd <https://schema.coypu.org/global#hasYear> ?vao_fd_year . " +
               "?vao_fd <https://schema.coypu.org/vtf#hasValueAddedOrigin> ?vao . " +
               "?vao rdf:type <https://schema.coypu.org/vtf#Vao> . " +
               "?vao <https://schema.coypu.org/vtf#hasIndustryCode> <https://data.coypu.org/classification/tiva-21/"+industryCode+"> . " +
               "?vao  <https://schema.coypu.org/vtf#hasTradeLocation> <https://data.coypu.org/country/"+location+"> . " +
               "?vao_fd <https://schema.coypu.org/vtf#hasFinalDemand> ?fd . " +
               "?fd rdf:type <https://schema.coypu.org/vtf#Fd> . " +
               "?fd <https://schema.coypu.org/vtf#hasIndustryCode> ?fdIndustryCode . " +
               "?fd <https://schema.coypu.org/vtf#hasTradeLocation> ?fdTradeLocation . " +
               "} LIMIT 5000000 ";
    }


}