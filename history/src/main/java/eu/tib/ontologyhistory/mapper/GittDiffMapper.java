package eu.tib.ontologyhistory.mapper;

import eu.tib.ontologyhistory.dto.git.GitDiffDto;
import eu.tib.ontologyhistory.model.GitDiff;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GittDiffMapper {
    GitDiffDto entityToDto(GitDiff gitDiff);

    List<GitDiffDto> entityToDto(Iterable<GitDiff> gitDiff);

    GitDiff dtoToEntity(GitDiffDto GitDiffDto);

    List<GitDiff> dtoToEntity(Iterable<GitDiffDto> GitDiffDto);
}
