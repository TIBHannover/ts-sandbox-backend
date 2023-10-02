package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.MappingController;
import eu.tib.ts.model.ontology.Mapping;
import eu.tib.ts.model.ontology.MappingModel;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import org.springframework.stereotype.Component;

@Component
public class MappingModelAssembler extends RepresentationModelAssemblerSupport<Mapping, MappingModel> {

    public MappingModelAssembler(){

    super(MappingController.class, MappingModel.class);

    }

    @Override
    public MappingModel toModel(Mapping entity) {

        MappingModel model = instantiateModel(entity);

        model.setName(entity.getName());

        model.setMappingDtoList(entity.getMappingDtoList());

        return model;

    }
}