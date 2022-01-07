package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.SimilarityController;
import eu.tib.ts.model.ontology.Similarity;
import eu.tib.ts.model.ontology.SimilarityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class SimilarityModelAssembler extends RepresentationModelAssemblerSupport<Similarity, SimilarityModel> {

    public SimilarityModelAssembler() {
        super(SimilarityController.class, SimilarityModel.class);
    }

    @Override
    public CollectionModel<SimilarityModel> toCollectionModel(Iterable<? extends Similarity> entities) {

        return super.toCollectionModel(entities);
    }

    @Override
    public SimilarityModel toModel(Similarity entity) {
        SimilarityModel model = instantiateModel(entity);
        model.setName(entity.getName());
        model.setOntologies(entity.getOntologies());

        return model;
    }

}
