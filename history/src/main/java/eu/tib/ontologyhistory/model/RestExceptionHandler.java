package eu.tib.ontologyhistory.model;

import eu.tib.ontologyhistory.controller.RequestDetails;
import eu.tib.ontologyhistory.dto.OntologyGitDiffRequest;
import eu.tib.ontologyhistory.model.exception.OntologyDefinitelyNotInPath;
import eu.tib.ontologyhistory.model.exception._UnloadableImportException;
import eu.tib.ontologyhistory.model.exception._UnparsableOntologyException;
import eu.tib.ontologyhistory.repository.InvalidDiffRepository;
import eu.tib.ontologyhistory.utils.ExceptionUtils;
import org.semanticweb.owlapi.model.UnloadableImportException;
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
import java.util.Arrays;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final InvalidDiffRepository invalidDiffRepository;

    @Inject
    private RequestDetails requestDetails;

    public RestExceptionHandler(InvalidDiffRepository invalidDiffRepository) {
        this.invalidDiffRepository = invalidDiffRepository;
    }

    @ExceptionHandler(_UnloadableImportException.class)
    protected ResponseEntity<Object> handleUnloadableImport(_UnloadableImportException ex, WebRequest request) {
        OntologyGitDiffRequest requestBody = (OntologyGitDiffRequest) request.getAttribute("requestBody", RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId("default")
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

    @ExceptionHandler(_UnparsableOntologyException.class)
    protected ResponseEntity<Object> handleUnparsableOntology(_UnparsableOntologyException ex, WebRequest request) {
        OntologyGitDiffRequest requestBody = (OntologyGitDiffRequest) request.getAttribute("requestBody", RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId("default")
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
        OntologyGitDiffRequest requestBody = (OntologyGitDiffRequest) request.getAttribute("requestBody", RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId("default")
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
