package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.CountryCodeController;
import eu.tib.tiva.controller.dto.CountryCodeModel;
import eu.tib.tiva.model.CountryCode;
import eu.tib.tiva.model.CountryCodesModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class CountryCodeModelAssembler extends RepresentationModelAssemblerSupport<CountryCode, CountryCodeModel> {

    public CountryCodeModelAssembler(){ super(CountryCodeController.class,CountryCodeModel.class); }

    @Override
    public CountryCodeModel toModel(CountryCode entity) {

        CountryCodeModel countryCodeModel = instantiateModel(entity);

        countryCodeModel.setId(entity.getId());
        countryCodeModel.setCountryCodeList(entity.getCountryCodeList());

        return countryCodeModel;
    }
}