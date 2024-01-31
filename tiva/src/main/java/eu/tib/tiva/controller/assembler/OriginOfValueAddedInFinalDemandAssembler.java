package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.OriginOfValueAddedInFinalDemandModel;
import eu.tib.tiva.model.OriginOfValueAddedInFinalDemand;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

public class OriginOfValueAddedInFinalDemandAssembler extends RepresentationModelAssemblerSupport<OriginOfValueAddedInFinalDemand, OriginOfValueAddedInFinalDemandModel> {


    public OriginOfValueAddedInFinalDemandAssembler(){ super(OriginOfValueAddedInFinalDemand.class,
            OriginOfValueAddedInFinalDemandModel.class); }

    @Override
    public OriginOfValueAddedInFinalDemandModel toModel(OriginOfValueAddedInFinalDemand entity) {

        OriginOfValueAddedInFinalDemandModel originOfValueAddedInFinalDemandModel =
                instantiateModel(entity);

        originOfValueAddedInFinalDemandModel.setId(entity.getId());
        originOfValueAddedInFinalDemandModel.setOriginOfValueAddedInFinalDemandList(entity.getOriginOfValueAddedInFinalDemandList());

        return originOfValueAddedInFinalDemandModel;

    }
}
