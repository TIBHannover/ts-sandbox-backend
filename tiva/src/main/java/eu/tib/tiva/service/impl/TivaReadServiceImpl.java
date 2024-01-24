package eu.tib.tiva.service.impl;

import eu.tib.tiva.service.TivaReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TivaReadServiceImpl implements TivaReadService {

    @Override
    public String readTivaSparqlQuery(String queryString) {

        return queryString;
    }

}
