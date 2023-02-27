package eu.tib.ts.controller.dto;

import lombok.*;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "mappings", itemRelation = "mapping")
public class PairwiseMappingModel extends RepresentationModel<PairwiseMappingModel> {

    private Pair<String,String> pair;
    String mappingSourceUri;
    String mappingTargetUri;


}