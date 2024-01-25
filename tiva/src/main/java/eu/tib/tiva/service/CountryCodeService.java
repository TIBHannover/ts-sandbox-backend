package eu.tib.tiva.service;

import eu.tib.tiva.model.CountryCode;
import eu.tib.tiva.model.CountryCodesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CountryCodeService {

    <T extends CountryCodesModel> Page<CountryCode> getCountryCodeList(String sparqlEndpoint,
                                                                       String userName,
                                                                       String password,
                                                                       Pageable pageable);
}
