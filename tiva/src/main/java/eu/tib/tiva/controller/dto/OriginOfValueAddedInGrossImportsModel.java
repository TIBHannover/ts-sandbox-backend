package eu.tib.tiva.controller.dto;

import eu.tib.tiva.model.OriginOfValueAddedInGrossImport;
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
public class OriginOfValueAddedInGrossImportsModel extends RepresentationModel<OriginOfValueAddedInGrossImportsModel> {

    String id;
    List<OriginOfValueAddedInGrossImport> originOfValueAddedInGrossImports;

}
