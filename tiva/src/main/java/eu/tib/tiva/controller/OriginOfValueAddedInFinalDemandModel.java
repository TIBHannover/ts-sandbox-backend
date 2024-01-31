package eu.tib.tiva.controller;
import eu.tib.tiva.model.OriginOfValueAdded;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "results", itemRelation = "results")
public class OriginOfValueAddedInFinalDemandModel extends RepresentationModel<OriginOfValueAddedInFinalDemandModel> {

    String id;
    List<OriginOfValueAdded> originOfValueAddedList;

}
