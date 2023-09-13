package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.OntologyDto;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "similarities", itemRelation = "similarity")
public class SimilarityModel extends RepresentationModel<SimilarityModel> {
    private String name;
    private List<OntologyDto> ontologies;
}
