package eu.tib.tiva.service;

import eu.tib.tiva.model.ValueAndTradeFlowCode;
import eu.tib.tiva.model.ValueAndTradeFlowCodesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ValueAndTradeFlowService {

    <T extends ValueAndTradeFlowCodesModel> Page<ValueAndTradeFlowCode> getValueAndTradeFlowCodeList(String sparqlEndpoint,
                                                                                                     String type,
                                                                                                     Pageable pageable);
}
