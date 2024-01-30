package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.TradeLocationController;
import eu.tib.tiva.controller.dto.TradeLocationModel;
import eu.tib.tiva.model.TradeLocationCode;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class CountryCodeModelAssembler extends RepresentationModelAssemblerSupport<TradeLocationCode, TradeLocationModel> {

    public CountryCodeModelAssembler(){ super(TradeLocationController.class, TradeLocationModel.class); }

    @Override
    public TradeLocationModel toModel(TradeLocationCode entity) {

        TradeLocationModel tradeLocationModel = instantiateModel(entity);

        tradeLocationModel.setId(entity.getId());
        tradeLocationModel.setTradeLocationCodeList(entity.getTradeLocationCodeList());

        return tradeLocationModel;
    }
}