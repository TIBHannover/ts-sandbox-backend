package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.dto.OriginOfValueAddedInGrossImportsModel;
import eu.tib.tiva.model.OriginOfValueAddedInGrossImports;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class OriginOfValueAddedInGrossImportsAssembler extends RepresentationModelAssemblerSupport<OriginOfValueAddedInGrossImports, OriginOfValueAddedInGrossImportsModel> {

    public OriginOfValueAddedInGrossImportsAssembler(){ super(OriginOfValueAddedInGrossImports.class,
            OriginOfValueAddedInGrossImportsModel.class); }

    @Override
    public OriginOfValueAddedInGrossImportsModel toModel(OriginOfValueAddedInGrossImports entity) {

        OriginOfValueAddedInGrossImportsModel originOfValueAddedInGrossImportsModel =
                instantiateModel(entity);

        originOfValueAddedInGrossImportsModel.setId(entity.getId());
        originOfValueAddedInGrossImportsModel.setOriginOfValueAddedInGrossImportList(entity.getOriginOfValueAddedInGrossImportList());

        return originOfValueAddedInGrossImportsModel;
    }
}
