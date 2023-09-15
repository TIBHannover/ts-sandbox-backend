package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.MappingController;
import eu.tib.ts.model.ontology.MappingGroupedBySourceOntology;
import eu.tib.ts.model.ontology.MappingGroupedBySourceOntologyModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class MappingGroupedBySourceOntologyModelAssembler extends RepresentationModelAssemblerSupport<MappingGroupedBySourceOntology, MappingGroupedBySourceOntologyModel>  {

    public MappingGroupedBySourceOntologyModelAssembler(){

        super(MappingController.class, MappingGroupedBySourceOntologyModel.class);

    }


    @Override
    public MappingGroupedBySourceOntologyModel toModel(MappingGroupedBySourceOntology entity) {

        MappingGroupedBySourceOntologyModel model = instantiateModel(entity);

        model.setName(entity.getName());
        model.setMappingGropedBySourceOntologyDtoList(entity.getMappingGropedBySourceOntologyDtoList());

        return null;
    }
}
