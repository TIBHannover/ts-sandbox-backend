package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.MostCommonlyUsedController;
import eu.tib.ts.controller.dto.KeyValueResultDto;
import eu.tib.ts.model.ontology.KeyValueModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class KeyValueModelAssembler extends RepresentationModelAssemblerSupport<KeyValueResultDto, KeyValueModel> {

    public KeyValueModelAssembler() {
        super(MostCommonlyUsedController.class, KeyValueModel.class);
    }

    @Override
    public KeyValueModel toModel(KeyValueResultDto entity) {
        KeyValueModel model = instantiateModel(entity);
        model.setKey(entity.getKey());
        model.setValue(entity.getValue());

        return model;
    }

}
