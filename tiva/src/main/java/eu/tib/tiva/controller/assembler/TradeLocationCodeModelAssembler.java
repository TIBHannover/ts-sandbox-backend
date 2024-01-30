package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.ValueAndTradeFlowController;
import eu.tib.tiva.controller.dto.ValueAndTradeFlowModel;
import eu.tib.tiva.model.ValueAndTradeFlowCode;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class TradeLocationCodeModelAssembler extends RepresentationModelAssemblerSupport<ValueAndTradeFlowCode, ValueAndTradeFlowModel> {

    public TradeLocationCodeModelAssembler(){ super(ValueAndTradeFlowController.class, ValueAndTradeFlowModel.class); }

    @Override
    public ValueAndTradeFlowModel toModel(ValueAndTradeFlowCode entity) {

        ValueAndTradeFlowModel valueAndTradeFlowModel = instantiateModel(entity);

        valueAndTradeFlowModel.setId(entity.getId());
        valueAndTradeFlowModel.setValueAndTradeFlowCodeList(entity.getValueAndTradeFlowCodeList());

        return valueAndTradeFlowModel;
    }
}