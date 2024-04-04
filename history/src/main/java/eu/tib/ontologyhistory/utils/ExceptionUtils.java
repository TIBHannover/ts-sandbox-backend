package eu.tib.ontologyhistory.utils;

import java.util.Objects;

import eu.tib.ontologyhistory.model.exception.UnloadableCustomImportException;
import eu.tib.ontologyhistory.model.exception.UnparsableCustomOntologyException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class ExceptionUtils {

    private ExceptionUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Throwable findRootCause(Throwable throwable) {
        Objects.requireNonNull(throwable);
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    public static boolean handleCustomException(Throwable throwable) {
        if (throwable instanceof UnloadableImportException) {
            throw new UnloadableCustomImportException(throwable.getMessage());
        }
        if (throwable instanceof UnparsableOntologyException) {
            throw new UnparsableCustomOntologyException(throwable.getMessage());
        }
        return false;
    }
}
