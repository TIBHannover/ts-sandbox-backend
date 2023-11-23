package eu.tib.ts.controller.dto;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "externalmappings", itemRelation = "externalmapping")
public class ExternalMappingModel extends RepresentationModel<ExternalMappingModel> {

    long id;
    String mappingId;
    Set<SourceOntologyObjectSetModel> sourceOntology;
    int numberOfTargetOntologies;
    Set<TargetOntologyObjectSetModel> targetOntologyList;

}