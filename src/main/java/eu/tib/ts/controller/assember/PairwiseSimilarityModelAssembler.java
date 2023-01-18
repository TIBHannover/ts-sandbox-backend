package eu.tib.ts.controller.assember;

import eu.tib.ts.controller.SimilarityController;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.controller.dto.PairwiseSimilarityModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class PairwiseSimilarityModelAssembler
    extends RepresentationModelAssemblerSupport<PairwiseSimilarity, PairwiseSimilarityModel> {

    public PairwiseSimilarityModelAssembler() {
        super(SimilarityController.class, PairwiseSimilarityModel.class);
    }

    @Override
    public PairwiseSimilarityModel toModel(PairwiseSimilarity entity) {
        PairwiseSimilarityModel model = instantiateModel(entity);
        model.setPair(entity.getPair());
        model.setSum(entity.getSum());
        model.setTotalSum(entity.getTotalSum());
        model.setPercentage(entity.getPercent());
        model.setTitles(entity.getTitles());
        model.setCharacteristics(entity.getCharacteristics());

        return model;
    }

}
