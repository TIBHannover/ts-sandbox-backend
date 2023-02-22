package eu.tib.ts.model.ontology;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation="mappings", itemRelation="mapping")
public class MappingModel extends RepresentationModel<MappingModel> {


}
