package eu.tib.tiva.service;

import eu.tib.tiva.model.CountryCode;
import eu.tib.tiva.model.CountryCodeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CountryCodeService {

    <T extends CountryCodeModel> Page<CountryCode> queryCountryCodes(String skyNetSparqlEndpoint,
                                                                                  String userName,
                                                                                  String password,
                                                                                  Pageable pageable);

}
