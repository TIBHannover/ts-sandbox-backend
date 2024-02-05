package eu.tib.tiva.service;

import eu.tib.tiva.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ValueAndTradeFlowService {

    <T extends ValueAndTradeFlowCodesModel> Page<ValueAndTradeFlowCode> getValueAndTradeFlowCodeList(String sparqlEndpoint,
                                                                                                     String type,
                                                                                                     Pageable pageable);

    <T extends OriginOfValueAddedInFinalDemandModel> Page<OriginOfValueAddedInFinalDemand> getOriginOfValueAddedList(String sparqlEndpoint,
                                                                                                                     String location,
                                                                                                                     String industry,
                                                                                                                     String queryString,
                                                                                                                     Pageable pageable);
    <T extends OriginOfValueAddedInGrossImportModel> Page<OriginOfValueAddedInGrossImports> getOriginOfValueAddedInGrossImports(String sparqlEndpoint,
                                                                                                                               String location,
                                                                                                                               String queryString,
                                                                                                                               Pageable pageable);

    <T extends OriginOfValueAddedInGrossImportModel> Page<OriginOfValueAddedInGrossImports>
    getGrossExportsByOriginOfValueAddedAndFinalDestination(String sparqlEndpoint,
                                                                  String location,
                                                                  String queryString,
                                                                  Pageable pageable);
}