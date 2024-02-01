package eu.tib.tiva.service.impl;

import eu.tib.tiva.model.*;
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
            if(type.equals("Country") || type.equals("InetrnationalOrganization") || type.equals("InternationalOrganization") ) {

            /**
            * Because of syntax typo that is made in Python code for class name InternationalOrganization
            * we added this if statement to fix it.
            */
            if(type.equals("InternationalOrganization")){

            type="InetrnationalOrganization";

            }

            tradeCodeStringList= getCodeStringList(conn, getTradeLocationCodesQuery(type));

            } else if(type.equals("IndustryCode")){

            tradeCodeStringList= getCodeStringList(conn, getIndustryCodesQuery());

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
    public <T extends OriginOfValueAddedInFinalDemandModel> Page<OriginOfValueAddedInFinalDemand> getOriginOfValueAddedList(String sparqlEndpoint,
                                                                                                                            String location,
                                                                                                                            String industry,
                                                                                                                            String queryString,
                                                                                                                            Pageable pageable) {
        log.info("query origin of value aded in (final demand or exports) ");

        Repository repo = new SPARQLRepository(sparqlEndpoint);

        repo.initialize();

        List<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandList = new ArrayList<>();

        try (RepositoryConnection conn = repo.getConnection()) {

            originOfValueAddedInFinalDemandList = getOriginOfValueAddedInFinalDemand(conn,
                    queryString);

        }catch (Exception e){

        e.printStackTrace();

        }

        repo.shutDown();

    return PageUtils.toPage(originOfValueAddedInFinalDemandList, pageable);

    }

    @Override
    public <T extends OriginOfValueAddedInGrossImportModel> Page<OriginOfValueAddedInGrossImports>
    getOriginOfValueAddedInGrossImports(String sparqlEndpoint, String location, String queryString, Pageable pageable) {


        log.info("query origin of value aded in gross imports");

        Repository repo = new SPARQLRepository(sparqlEndpoint);

        repo.initialize();

        List<OriginOfValueAddedInGrossImports>  originOfValueAddedInGrossImportsList = new ArrayList<>();

        try (RepositoryConnection conn = repo.getConnection()) {

            originOfValueAddedInGrossImportsList = getOriginOfValueAddedInGrossImportQuery(conn,
                    queryString);

        }catch (Exception e){

            e.printStackTrace();

        }

    repo.shutDown();

    return PageUtils.toPage(originOfValueAddedInGrossImportsList, pageable);

    }

    private List<OriginOfValueAddedInGrossImports> getOriginOfValueAddedInGrossImportQuery(
            RepositoryConnection conn,
            String queryString){

        List<OriginOfValueAddedInGrossImport> originOfValueAddedInGrossImportList = new ArrayList<>();

        List<OriginOfValueAddedInGrossImports>  originOfValueAddedInGrossImportsList = new ArrayList<>();

        TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);

        try (TupleQueryResult result = tupleQuery.evaluate()) {

        Set<BindingSet> resultList = QueryResults.asSet(result);

        for (BindingSet bindingSet : resultList) {

            List<ValueAndTradeFlow> grossExports = new ArrayList<>();

            //?exTradeLocation ?exIndustryCode ?importTradeLocation ?vao_import_value ?vao_import_year

            Value  exTradeLocation = bindingSet.getValue("exTradeLocation");
            Value  exIndustryCode = bindingSet.getValue("exIndustryCode");
            Value  importTradeLocation = bindingSet.getValue("importTradeLocation");
            Value  vao_import_value = bindingSet.getValue("vao_import_value");
            Value  vao_import_year = bindingSet.getValue("vao_import_year");

            ValueAndTradeFlow valueAndTradeFlow = new ValueAndTradeFlow(
                    exTradeLocation.stringValue(),
                    exIndustryCode.stringValue());

            grossExports.add(valueAndTradeFlow);

            OriginOfValueAddedInGrossImport originOfValueAddedInGrossImport = new OriginOfValueAddedInGrossImport();

            originOfValueAddedInGrossImport.setGrossExports(grossExports);
            originOfValueAddedInGrossImport.setValue(vao_import_value.stringValue());
            originOfValueAddedInGrossImport.setYear(vao_import_year.stringValue());
            originOfValueAddedInGrossImport.setImportCountryCode(importTradeLocation.stringValue());

            originOfValueAddedInGrossImportList.add(originOfValueAddedInGrossImport);

            }

            OriginOfValueAddedInGrossImports originOfValueAddedInGrossImports = processOriginOfValueAddedInGrossImports(
                    UUID.randomUUID().toString(),
                    originOfValueAddedInGrossImportList
            );

            originOfValueAddedInGrossImportsList.add(originOfValueAddedInGrossImports);

        }

    return  originOfValueAddedInGrossImportsList;

    }

    private List<OriginOfValueAddedInFinalDemand> getOriginOfValueAddedInFinalDemand(RepositoryConnection conn,
                                                                                     String queryString){

        List<OriginOfValueAdded> originOfValueAddedList = new ArrayList<>();

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

                OriginOfValueAdded originOfValueAdded = new OriginOfValueAdded();

                originOfValueAdded.setLocationCode(fdTradeLocation.stringValue());
                originOfValueAdded.setIndustryCode(fdIndustryCode.stringValue());
                originOfValueAdded.setValue(vao_fd_value.stringValue());
                originOfValueAdded.setYear(vao_fd_year.stringValue());

                originOfValueAddedList.add(originOfValueAdded);

            }

            OriginOfValueAddedInFinalDemand originOfValueAddedInFinalDemand = processOriginOfValueAddedInFinalDemand(
                    UUID.randomUUID().toString(),
                    originOfValueAddedList
            );

            originOfValueAddedInFinalDemandList.add(processOriginOfValueAddedInFinalDemand(
                    UUID.randomUUID().toString(),
                    originOfValueAddedList
            ));

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

    private  OriginOfValueAddedInGrossImports processOriginOfValueAddedInGrossImports(String id,
                                                                                      List<OriginOfValueAddedInGrossImport> originOfValueAddedInGrossImportList){

        return OriginOfValueAddedInGrossImports.builder()
                .id(id)
                .originOfValueAddedInGrossImportList(originOfValueAddedInGrossImportList)
                .build();
    }
    private OriginOfValueAddedInFinalDemand processOriginOfValueAddedInFinalDemand(String id,
                                                                                   List<OriginOfValueAdded> originOfValueAddedInFinalDemandList){
        return OriginOfValueAddedInFinalDemand.builder()
                .id(id)
                .originOfValueAddedList(originOfValueAddedInFinalDemandList)
                .build();

    }

    private ValueAndTradeFlowCode processValueAndTradeFlowCode(String id,
                                                               List<String> tradeCodeList) {
        return ValueAndTradeFlowCode.builder()
                .id(id)
                .valueAndTradeFlowCodeList(tradeCodeList)
                .build();
    }

    public static String getTradeLocationCodesQuery(String type) {

        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               "SELECT DISTINCT ?code  " +
               "WHERE { " +
               "?s  <https://schema.coypu.org/vtf#hasTradeLocation> ?code . " +
               "?code rdf:type <https://schema.coypu.org/global#"+type+"> . " +
               "} LIMIT 5000 ";
    }

    public static String getIndustryCodesQuery(){

        return "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
               "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
               "SELECT DISTINCT ?code " +
               "WHERE { " +
               "?s <https://schema.coypu.org/vtf#hasIndustryCode> ?code . " +
               "} LIMIT 5000 ";
    }

}