package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.CharacteristicsInfo;
import eu.tib.ts.model.ontology.Titles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "similarities", itemRelation = "similarity")
public class PairwiseSimilarityModel extends RepresentationModel<PairwiseSimilarityModel> {
    private Pair<String, String> pair;
    private double sum;
    private double totalSum;
    private double percentage;
    private Pair<String, String> titles;
    private Map<String, CharacteristicsInfo> characteristics;
}
