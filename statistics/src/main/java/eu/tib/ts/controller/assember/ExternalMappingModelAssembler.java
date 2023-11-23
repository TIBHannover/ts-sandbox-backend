package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.ExternalMappingController;
import eu.tib.ts.controller.dto.ExternalMappingModel;
import eu.tib.ts.model.external.mapping.ExternalMapping;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ExternalMappingModelAssembler extends RepresentationModelAssemblerSupport<ExternalMapping, ExternalMappingModel> {

    public ExternalMappingModelAssembler(){ super(ExternalMappingController.class,ExternalMappingModel.class); }

    @Override
    public ExternalMappingModel toModel(ExternalMapping entity) {

        ExternalMappingModel externalMappingModel = instantiateModel(entity);

        externalMappingModel.setMappingId(entity.getMappingId());
        externalMappingModel.setSourceOntologyURI(entity.getSourceOntologyURI());
        externalMappingModel.setNumberOfTargetOntologies(entity.getNumberOfTargetOntologies());
        externalMappingModel.setTargetOntologyList(entity.getTargetOntologyList());

        return externalMappingModel;
    }
}