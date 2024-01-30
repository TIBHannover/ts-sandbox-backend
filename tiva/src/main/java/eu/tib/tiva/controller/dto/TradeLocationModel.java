package eu.tib.tiva.controller.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "countrycodes", itemRelation = "countrycodes")
public class TradeLocationModel extends RepresentationModel<TradeLocationModel> {

    String id;
    List<String> tradeLocationCodeList;
}
