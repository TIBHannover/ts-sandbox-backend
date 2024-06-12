//package eu.tib.ontologyhistory.service;
//
//import eu.tib.ontologyhistory.dto.diff.DiffDto;
//import eu.tib.ontologyhistory.dto.ontology.OntologyDto;
//import eu.tib.ontologyhistory.mapper.DiffMapper;
//import eu.tib.ontologyhistory.mapper.OntologyMapper;
//import eu.tib.ontologyhistory.model.ApiError;
//import eu.tib.ontologyhistory.model.CommitStatus;
//import eu.tib.ontologyhistory.model.Diff;
//import eu.tib.ontologyhistory.model.Ontology;
//import eu.tib.ontologyhistory.repository.OntologyRepository;
//import eu.tib.ontologyhistory.service.network.GithubService;
//import lombok.AllArgsConstructor;
//import lombok.val;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@Service
//@AllArgsConstructor
//public class OntologyService {
//
//    private final OntologyRepository ontologyRepository;
//
//    private final RobotService robotService;
//
//    private final ApiErrorService apiErrorService;
//
//    private final OntologyMapper ontologyMapper;
//
//    private final DiffMapper diffMapper;
//
//    private final GithubService githubService;
//
//    public List<OntologyDto> findAll() {
//        val ontologies = ontologyRepository.findAll();
//        return ontologyMapper.entityToDto(ontologies);
//    }
//
//    public OntologyDto findById(String id) {
//        val ontology = ontologyRepository.findById(id).orElse(null);
//        return ontologyMapper.entityToDto(ontology);
//    }
//
//    public Object findByUrl(String url) {
//        return ontologyRepository.findByUrl(url).orElse(null);
//    }
//
//    public void insert(OntologyDto ontologyDto) {
//        val ontology = ontologyMapper.dtoToEntity(ontologyDto);
//        val savedOntology = ontologyRepository.save(ontology);
//        apiErrorService.assignOntologyId(ontology.getInvalidDiffs(), savedOntology.getId());
//        ontologyRepository.save(savedOntology);
//    }
//
//    public OntologyDto create(String url) {
//        val diffAdds = githubService.getDiffAdds(url);
//        val diffs = new ArrayList<DiffDto>();
//        for (val diffAdd : diffAdds) {
//            val diff = robotService.makeDiffFromGit(diffAdd, "test");
//            if (diff != null) {
//                diffs.add(diff);
//            }
//        }
//
//        Collections.reverse(diffs);
//
//        if (!diffs.isEmpty()) {
//            val ontology = Ontology.builder()
//                    .url(url)
//                    .name("placeholder ontology name")
//                    .description("placeholder ontology description")
//                    .diffs(diffMapper.dtoToEntity(diffs))
//                    .invalidDiffs(Collections.emptyList())
//                    .atime(diffMapper.dtoToEntity(diffs).get(0).getDatetime())
//                    .commitStatus(new CommitStatus("latest", "0", "main"))
//                    .type("github")
//                    .build();
//
//            ontologyRepository.save(ontology);
//            return ontologyMapper.entityToDto(ontology);
//        }
//        return null;
//    }
//
//    public void update(String id, OntologyDto ontologyDto) {
//        val ontology = ontologyRepository.findById(id).orElse(null);
//        assert ontology != null;
//        ontologyMapper.updateEntityFromDto(ontologyDto, ontology);
//        val latestDiffTime = calculateLatestDiffTime(ontology.getDiffs(), ontology.getInvalidDiffs());
//        ontology.setAtime(latestDiffTime);
//        ontologyRepository.save(ontology);
//    }
//
//    public void edit(String id, OntologyDto ontologyDto) {
//        val ontology = ontologyRepository.findById(id).orElse(null);
//        assert ontology != null;
//        ontologyMapper.editEntityFromDto(ontologyDto, ontology);
//        ontologyRepository.save(ontology);
//    }
//    private Instant calculateLatestDiffTime(List<Diff> diffs, List<ApiError> invalidDiffs) {
//        Instant latestDiffTime = diffs.isEmpty() ? Instant.MIN : diffs.get(0).getDatetime();
//        Instant latestInvalidDiffTime = invalidDiffs.isEmpty() ? Instant.MIN : invalidDiffs.get(0).getTimestamp();
//        return latestDiffTime.isAfter(latestInvalidDiffTime) ? latestDiffTime : latestInvalidDiffTime;
//    }
//
//    public void deleteById(String id) {
//        ontologyRepository.deleteById(id);
//    }
//
//    public List<Diff> getDiffsBetween(String ontologyId, Instant startDate, Instant endDate) {
//        val ontology = ontologyRepository.findById(ontologyId).orElse(null);
//        List<Diff> filteredDiffs = new ArrayList<>();
//        assert ontology != null;
//        for (Diff diff : ontology.getDiffs()) {
//            if (diff.getDatetime().isAfter(startDate) && diff.getParentDatetime().isBefore(endDate)) {
//                filteredDiffs.add(diff);
//            }
//        }
//
//        return filteredDiffs;
//    }
//
//}
