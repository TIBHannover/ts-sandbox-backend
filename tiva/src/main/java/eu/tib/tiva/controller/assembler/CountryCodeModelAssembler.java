package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.TradeLocationController;
import eu.tib.tiva.controller.dto.CountryCodeModel;
import eu.tib.tiva.model.TradeLocationCode;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class CountryCodeModelAssembler extends RepresentationModelAssemblerSupport<TradeLocationCode, CountryCodeModel> {

    public CountryCodeModelAssembler(){ super(TradeLocationController.class,CountryCodeModel.class); }

    @Override
    public CountryCodeModel toModel(TradeLocationCode entity) {

        CountryCodeModel countryCodeModel = instantiateModel(entity);

        countryCodeModel.setId(entity.getId());
        countryCodeModel.setCountryCodeList(entity.getTradeLocationList());

        return countryCodeModel;
    }
}