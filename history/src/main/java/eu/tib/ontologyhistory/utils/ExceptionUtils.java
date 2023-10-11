package eu.tib.ontologyhistory.utils;

import java.util.Objects;

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
}
