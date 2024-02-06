package eu.tib.tiva.controller.assembler;

import eu.tib.tiva.controller.dto.GrossExportByOriginOfValueAddedAndFinalDestinationModel;
import eu.tib.tiva.model.GrossExportByOriginOfValueAddedAndFinalDestinations;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class GrossExportsByOriginOfValueAddedAndFinalDestinationAssembler extends
        RepresentationModelAssemblerSupport<GrossExportByOriginOfValueAddedAndFinalDestinations, GrossExportByOriginOfValueAddedAndFinalDestinationModel> {

    public GrossExportsByOriginOfValueAddedAndFinalDestinationAssembler(){
        super(GrossExportByOriginOfValueAddedAndFinalDestinations.class,
                GrossExportByOriginOfValueAddedAndFinalDestinationModel.class
        );

    }

    @Override
    public GrossExportByOriginOfValueAddedAndFinalDestinationModel toModel(GrossExportByOriginOfValueAddedAndFinalDestinations entity) {

        GrossExportByOriginOfValueAddedAndFinalDestinationModel grossExportByOriginOfValueAddedAndFinalDestinationModel =
                instantiateModel(entity);

        grossExportByOriginOfValueAddedAndFinalDestinationModel.setId(entity.getId());
        grossExportByOriginOfValueAddedAndFinalDestinationModel.setGrossExportByOriginOfValueAddedAndFinalDestinationList(
                entity.getGrossExportByOriginOfValueAddedAndFinalDestinationList());

        return grossExportByOriginOfValueAddedAndFinalDestinationModel;


    }
}
