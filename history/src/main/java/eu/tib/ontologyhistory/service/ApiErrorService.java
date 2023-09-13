package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.ApiError;
import eu.tib.ontologyhistory.model.Diff;
import eu.tib.ontologyhistory.repository.DiffRepository;
import eu.tib.ontologyhistory.repository.InvalidDiffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiErrorService {

    private final InvalidDiffRepository invalidDiffRepository;
    public ApiErrorService(InvalidDiffRepository invalidDiffRepository) {
        this.invalidDiffRepository = invalidDiffRepository;
    }
    public void assignOntologyId(List<ApiError> apiErrors, String ontologyId) {
        for (ApiError d : apiErrors) {
            ApiError apiError = invalidDiffRepository.findById(d.getId()).orElse(null);
            if (apiError != null) {
                apiError.setOntologyId(ontologyId);
                invalidDiffRepository.save(apiError);
            }
        }
    }
}
