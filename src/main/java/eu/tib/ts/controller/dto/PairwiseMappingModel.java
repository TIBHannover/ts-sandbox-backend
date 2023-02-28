package eu.tib.ts.controller.dto;

import eu.tib.ts.model.ontology.MappingCharacteristicsInfo;
import lombok.*;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "mappings", itemRelation = "mapping")
public class PairwiseMappingModel extends RepresentationModel<PairwiseMappingModel> {

    private Pair<String,String> pair;
    private double sum;
    private Pair<String, String> title;
    private Map<String, MappingCharacteristicsInfo> mappingCharacteristicsInfo;

}