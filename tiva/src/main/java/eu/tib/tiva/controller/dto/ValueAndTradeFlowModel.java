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
@Relation(collectionRelation = "valueandtradeflowcodes", itemRelation = "valueandtradeflowcodes")
public class ValueAndTradeFlowModel extends RepresentationModel<ValueAndTradeFlowModel> {

    String id;
    List<String> valueAndTradeFlowCodeList;
}
