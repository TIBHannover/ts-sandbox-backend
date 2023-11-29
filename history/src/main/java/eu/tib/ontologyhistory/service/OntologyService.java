package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.model.Ontology;
import eu.tib.ontologyhistory.repository.OntologyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OntologyService {

    private final OntologyRepository ontologyRepository;

    private final DiffService diffService;

    private final ApiErrorService apiErrorService;

    public OntologyService(OntologyRepository ontologyRepository, DiffService diffService, ApiErrorService apiErrorService) {
        this.ontologyRepository = ontologyRepository;
        this.diffService = diffService;
        this.apiErrorService = apiErrorService;
    }

    public List<Ontology> findAll() {
        return ontologyRepository.findAll();
    }

    public Ontology findById(String id) {
        return ontologyRepository.findById(id).orElse(null);
    }


    public Ontology insert(Ontology ontology) {
        Ontology savedOntology = ontologyRepository.save(ontology);
        diffService.assignOntologyId(ontology.getDiffs(), savedOntology.getId());
        apiErrorService.assignOntologyId(ontology.getInvalidDiffs(), savedOntology.getId());
        ontologyRepository.save(savedOntology);
        return savedOntology;
    }


    public ResponseEntity<String> deleteById(String id) {
        try {
            ontologyRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error happened on a server", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Diff> getDiffsBetween(String ontologyId, Instant startDate, Instant endDate) {
        Ontology ontology = ontologyRepository.findById(ontologyId).orElseThrow();
        List<Diff> filteredDiffs = new ArrayList<>();
        for (Diff diff : ontology.getDiffs()) {
            if (diff.getShaOffsetDateTime().isAfter(startDate) && diff.getParentOffsetDateTime().isBefore(endDate)) {
                filteredDiffs.add(diff);
            }
        }

        return filteredDiffs;
    }

}
