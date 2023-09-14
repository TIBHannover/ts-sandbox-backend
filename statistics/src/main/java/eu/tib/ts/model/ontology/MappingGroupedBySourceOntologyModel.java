package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingDto;
import eu.tib.ts.controller.dto.MappingGropedBySourceOntologyDto;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "mappings_grouped_by_source_ontology", itemRelation = "mapping_grouped_by_source_ontology")
public class MappingGroupedBySourceOntologyModel extends RepresentationModel<MappingGroupedBySourceOntologyModel> {

    private String name;
    private List<MappingGropedBySourceOntologyDto> mappingGropedBySourceOntologyDtoList;
}
