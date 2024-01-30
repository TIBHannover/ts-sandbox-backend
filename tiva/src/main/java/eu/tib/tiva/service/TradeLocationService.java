package eu.tib.tiva.service;

import eu.tib.tiva.model.TradeLocationCode;
import eu.tib.tiva.model.CountryCodesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeLocationService {

    <T extends CountryCodesModel> Page<TradeLocationCode> getCountryCodeList(String sparqlEndpoint,
                                                                             String type,
                                                                             Pageable pageable);
}
