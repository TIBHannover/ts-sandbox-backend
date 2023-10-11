package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.dto.LeftRightOntologies;
import eu.tib.ontologyhistory.model.Ontology;
import eu.tib.ontologyhistory.repository.OntologyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        ontology.setAtime(ontology.getDiffs().get(0).getShaOffsetDateTime());
        ontologyRepository.save(ontology);
        diffService.assignOntologyId(ontology.getDiffs(), ontology.getId());
        apiErrorService.assignOntologyId(ontology.getInvalidDiffs(), ontology.getId());
        return ontology;
    }

    public ResponseEntity<String> deleteById(String id) {
        try {
            ontologyRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error happened on a server", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
