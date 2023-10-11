package eu.tib.ontologyhistory.model;

import eu.tib.ontologyhistory.controller.RequestDetails;
import eu.tib.ontologyhistory.dto.OntologyGitDiffRequest;
import eu.tib.ontologyhistory.model.exception.UnloadableCustomImportException;
import eu.tib.ontologyhistory.model.exception.UnparsableCustomOntologyException;
import eu.tib.ontologyhistory.repository.InvalidDiffRepository;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.inject.Inject;

import java.time.LocalDateTime;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String REQUEST_BODY = "requestBody";

    private static final String DEFAULT_ONTOLOGY_ID = "default";
    private final InvalidDiffRepository invalidDiffRepository;

    @Inject
    private RequestDetails requestDetails;

    public RestExceptionHandler(InvalidDiffRepository invalidDiffRepository) {
        this.invalidDiffRepository = invalidDiffRepository;
    }

    @ExceptionHandler(UnloadableCustomImportException.class)
    protected ResponseEntity<Object> handleUnloadableImport(UnloadableCustomImportException ex, WebRequest request) {
        OntologyGitDiffRequest requestBody = (OntologyGitDiffRequest) request.getAttribute(REQUEST_BODY, RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.FAILED_DEPENDENCY.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("One or more resources were not loaded. Check left- or right- IRI Files")
                .timestamp(LocalDateTime.now())
                .leftIriFile(requestBody.getGitUrlLeft())
                .rightIriFile(requestBody.getGitUrlRight())
                .build();

        invalidDiffRepository.insert(response);
        return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
    }

    @ExceptionHandler(UnparsableCustomOntologyException.class)
    protected ResponseEntity<Object> handleUnparsableOntology(UnparsableCustomOntologyException ex, WebRequest request) {
        OntologyGitDiffRequest requestBody = (OntologyGitDiffRequest) request.getAttribute(REQUEST_BODY, RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("Error happened while parsing an ontology. Check left- or right- IRI Files")
                .timestamp(LocalDateTime.now())
                .leftIriFile(requestBody.getGitUrlLeft())
                .rightIriFile(requestBody.getGitUrlRight())
                .build();

        invalidDiffRepository.insert(response);
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        OntologyGitDiffRequest requestBody = (OntologyGitDiffRequest) request.getAttribute(REQUEST_BODY, RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("Some error happened during diff creation")
                .timestamp(LocalDateTime.now())
                .leftIriFile(requestBody.getGitUrlLeft())
                .rightIriFile(requestBody.getGitUrlRight())
                .build();

        invalidDiffRepository.insert(response);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
