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

    @Override
    public <T extends CountryCodesModel> Page<CountryCode> queryCountryCodes(String skyNetSparqlEndpoint,
                                                                             String userName,
                                                                             String password,
                                                                             Pageable pageable) {
        return null;
    }
}