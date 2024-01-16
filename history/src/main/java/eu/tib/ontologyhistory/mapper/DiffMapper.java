package eu.tib.ontologyhistory.mapper;

import eu.tib.ontologyhistory.dto.diff.DiffDto;
import eu.tib.ontologyhistory.model.Diff;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiffMapper {

    DiffDto entityToDto(Diff diff);

    List<DiffDto> entityToDto(Iterable<Diff> diff);

    Diff dtoToEntity(DiffDto diffDto);

    List<Diff> dtoToEntity(Iterable<DiffDto> diffDto);
}
