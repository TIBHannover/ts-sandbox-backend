package eu.tib.ontologyhistory.mapper;

import eu.tib.ontologyhistory.dto.ontology.OntologyDto;
import eu.tib.ontologyhistory.model.CommitStatus;
import eu.tib.ontologyhistory.model.Ontology;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OntologyMapper {

    OntologyDto entityToDto(Ontology ontology);

    List<OntologyDto> entityToDto(Iterable<Ontology> ontologies);

    Ontology dtoToEntity(OntologyDto ontology);

    List<Ontology> dtoToEntity(Iterable<OntologyDto> ontologies);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "diffs", target = "diffs")
    void updateEntityFromDto(OntologyDto ontologyDto, @MappingTarget Ontology ontology);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    void editEntityFromDto(OntologyDto ontologyDto, @MappingTarget Ontology ontology);
}
