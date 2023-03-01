package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.MappingController;
import eu.tib.ts.controller.dto.PairwiseMappingModel;
import eu.tib.ts.model.ontology.PairwiseMapping;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class PairwiseMappingModelAssembler
        extends RepresentationModelAssemblerSupport<PairwiseMapping, PairwiseMappingModel> {

    public PairwiseMappingModelAssembler() {
        super(MappingController.class, PairwiseMappingModel.class);
    }

    @Override
    public PairwiseMappingModel toModel(PairwiseMapping entity) {

        PairwiseMappingModel model = instantiateModel(entity);

        model.setPair(model.getPair());
        model.setSum(model.getSum());
        model.setMappingCharacteristicsInfo(model.getMappingCharacteristicsInfo());
        model.setTitle(model.getTitle());

        return model;
    }

}