package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingDto;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "mappings", itemRelation = "mapping")
public class MappingModel extends RepresentationModel<MappingModel> {
    private String name;
    private List<MappingDto> mappingDtoList;
}