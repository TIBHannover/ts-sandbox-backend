package eu.tib.ontologyhistory.model;

import eu.tib.ontologyhistory.dto.diff.DiffAdd;
import eu.tib.ontologyhistory.model.exception.RobotDiffExecutionException;
import eu.tib.ontologyhistory.model.exception.UnloadableCustomImportException;
import eu.tib.ontologyhistory.model.exception.UnparsableCustomOntologyException;
import eu.tib.ontologyhistory.model.exception.conto.ContoDiffExecutionException;
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

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String REQUEST_BODY = "requestBody";

    private static final String DEFAULT_ONTOLOGY_ID = "default";
    private final InvalidDiffRepository invalidDiffRepository;

    public RestExceptionHandler(InvalidDiffRepository invalidDiffRepository) {
        this.invalidDiffRepository = invalidDiffRepository;
    }

    @ExceptionHandler(UnloadableCustomImportException.class)
    protected ResponseEntity<Object> handleUnloadableImport(UnloadableCustomImportException ex, WebRequest request) {
        DiffAdd requestBody = (DiffAdd) request.getAttribute(REQUEST_BODY, RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.FAILED_DEPENDENCY.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("One or more resources were not loaded. Check left- or right- IRI Files")
                .timestamp(requestBody.datetime())
                .leftIriFile(requestBody.gitUrlLeft())
                .rightIriFile(requestBody.gitUrlRight())
                .build();

        invalidDiffRepository.insert(response);
        return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
    }

    @ExceptionHandler(UnparsableCustomOntologyException.class)
    protected ResponseEntity<Object> handleUnparsableOntology(UnparsableCustomOntologyException ex, WebRequest request) {
        DiffAdd requestBody = (DiffAdd) request.getAttribute(REQUEST_BODY, RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("Error happened while parsing an ontology. Check left- or right- IRI Files")
                .timestamp(requestBody.datetime())
                .leftIriFile(requestBody.gitUrlLeft())
                .rightIriFile(requestBody.gitUrlRight())
                .build();

        invalidDiffRepository.insert(response);
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(RobotDiffExecutionException.class)
    protected ResponseEntity<Object> handleRobotDiffExecution(RobotDiffExecutionException ex, WebRequest request) {
        DiffAdd requestBody = (DiffAdd) request.getAttribute(REQUEST_BODY, RequestAttributes.SCOPE_REQUEST);
        assert requestBody != null;
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("Error happened while parsing an ontology. Check left- or right- IRI Files")
                .timestamp(requestBody.datetime())
                .leftIriFile(requestBody.gitUrlLeft())
                .rightIriFile(requestBody.gitUrlRight())
                .build();

        invalidDiffRepository.insert(response);
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @ExceptionHandler(ContoDiffExecutionException.class)
    protected ResponseEntity<Object> handleContoDiffExecution(ContoDiffExecutionException ex) {
        ApiError response = ApiError.builder()
                .ontologyId(DEFAULT_ONTOLOGY_ID)
                .status(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase())
                .debugMessage(ex.getMessage())
                .message("Error happened while parsing an ontology. Check left- or right- IRI Files")
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
