package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.ontology.OntologyDto;
import eu.tib.ontologyhistory.mapper.OntologyMapper;
import eu.tib.ontologyhistory.model.ApiError;
import eu.tib.ontologyhistory.model.CommitStatus;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.Ontology;
import eu.tib.ontologyhistory.repository.OntologyRepository;
import eu.tib.ontologyhistory.service.network.GithubService;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class OntologyService {

    private final OntologyRepository ontologyRepository;

    private final DiffService diffService;

    private final ApiErrorService apiErrorService;

    private final OntologyMapper ontologyMapper;

    private final GithubService githubService;

    public List<OntologyDto> findAll() {
        val ontologies = ontologyRepository.findAll();
        return ontologyMapper.entityToDto(ontologies);
    }

    public OntologyDto findById(String id) {
        val ontology = ontologyRepository.findById(id).orElse(null);
        return ontologyMapper.entityToDto(ontology);
    }

    public void insert(OntologyDto ontologyDto) {
        val ontology = ontologyMapper.dtoToEntity(ontologyDto);
        val savedOntology = ontologyRepository.save(ontology);
        diffService.assignOntologyId(ontology.getDiffs(), savedOntology.getId());
        apiErrorService.assignOntologyId(ontology.getInvalidDiffs(), savedOntology.getId());
        ontologyRepository.save(savedOntology);
    }

    public void create(OntologyDto ontologyDto) throws Exception {
        val diffAdds = githubService.getDiffAdds(ontologyDto);
        val diffs = new ArrayList<Diff>();
        for (val diffAdd : diffAdds) {
            val diff = diffService.makeDiffFromGit(diffAdd);
            if (diff != null) {
                diffs.add(diff);
            }
        }

        if (!diffs.isEmpty()) {
            val ontology = Ontology.builder()
                    .url(ontologyDto.url())
                    .name(ontologyDto.name())
                    .description(ontologyDto.description())
                    .diffs(diffs)
                    .invalidDiffs(Collections.EMPTY_LIST)
                    .atime(diffs.get(0).getTimestamp())
                    .commitStatus(new CommitStatus("latest", "0", "main"))
                    .type("github")
                    .build();

            val savedOntology = ontologyRepository.save(ontology);
//            diffService.assignOntologyId(ontology.getDiffs(), savedOntology.getId());
//            apiErrorService.assignOntologyId(ontology.getInvalidDiffs(), savedOntology.getId());
            ontologyRepository.save(savedOntology);
        }
    }

    public void update(String id, OntologyDto ontologyDto) {
        val ontology = ontologyRepository.findById(id).orElse(null);
        assert ontology != null;
        ontologyMapper.updateEntityFromDto(ontologyDto, ontology);
        val latestDiffTime = calculateLatestDiffTime(ontology.getDiffs(), ontology.getInvalidDiffs());
        ontology.setAtime(latestDiffTime);
        ontologyRepository.save(ontology);
    }

    public void edit(String id, OntologyDto ontologyDto) {
        val ontology = ontologyRepository.findById(id).orElse(null);
        assert ontology != null;
        ontologyMapper.editEntityFromDto(ontologyDto, ontology);
        ontologyRepository.save(ontology);
    }
    private Instant calculateLatestDiffTime(List<Diff> diffs, List<ApiError> invalidDiffs) {
        Instant latestDiffTime = diffs.isEmpty() ? Instant.MIN : diffs.get(0).getShaOffsetDateTime();
        Instant latestInvalidDiffTime = invalidDiffs.isEmpty() ? Instant.MIN : invalidDiffs.get(0).getTimestamp();
        return latestDiffTime.isAfter(latestInvalidDiffTime) ? latestDiffTime : latestInvalidDiffTime;
    }

    public void deleteById(String id) {
        ontologyRepository.deleteById(id);
    }

    public List<Diff> getDiffsBetween(String ontologyId, Instant startDate, Instant endDate) {
        val ontology = ontologyRepository.findById(ontologyId).orElse(null);
        List<Diff> filteredDiffs = new ArrayList<>();
        for (Diff diff : ontology.getDiffs()) {
            if (diff.getShaOffsetDateTime().isAfter(startDate) && diff.getParentOffsetDateTime().isBefore(endDate)) {
                filteredDiffs.add(diff);
            }
        }

        return filteredDiffs;
    }

}
