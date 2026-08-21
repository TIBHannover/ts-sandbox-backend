package eu.tib.ontologyhistory.service.robot;

import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RobotDiffFailureClassifier {

    private static final int MAX_MESSAGE_LENGTH = 2_000;

    private static final int MONGODB_DOCUMENT_LIMIT_BYTES = 16_777_216;

    private static final Pattern IMPORT_IRI_PATTERN = Pattern.compile("imported ontology: <([^>]+)>");

    private static final Pattern HTTP_STATUS_PATTERN = Pattern.compile("HTTP response code: (\\d+)");

    private RobotDiffFailureClassifier() {
        throw new IllegalStateException("Utility class");
    }

    public static RobotDiffFailure classify(Exception exception) {
        List<Throwable> causes = causes(exception);
        String combinedMessage = combinedMessage(causes);
        Throwable mostSpecific = causes.get(causes.size() - 1);

        UnloadableImportException importException = findCause(causes, UnloadableImportException.class);
        if (importException != null) {
            return classifyImportFailure(importException, combinedMessage);
        }

        if (findCause(causes, UnparsableOntologyException.class) != null || isParseMessage(combinedMessage)) {
            return failure(RobotDiffFailureCode.ONTOLOGY_PARSE_ERROR,
                    "ROBOT could not parse one of the ontology versions for this commit. "
                            + "The downloaded file at this commit may not be valid RDF/OWL, may be empty, or may contain HTML/error content instead of ontology content. "
                            + "Technical detail: " + firstUsefulMessage(mostSpecific, exception));
        }

        if (isMongoDocumentSizeFailure(combinedMessage)) {
            return outputTooLarge(MONGODB_DOCUMENT_LIMIT_BYTES);
        }

        return failure(RobotDiffFailureCode.UNKNOWN,
                "ROBOT diff failed before a semantic diff could be stored. Technical detail: "
                        + firstUsefulMessage(mostSpecific, exception));
    }

    public static RobotDiffFailure outputTooLarge(int maxMongoDocumentBytes) {
        return failure(RobotDiffFailureCode.OUTPUT_TOO_LARGE,
                String.format("ROBOT calculated a semantic diff, but the markdown output was larger than the MongoDB document limit (%d bytes).",
                        maxMongoDocumentBytes));
    }

    public static RobotDiffFailure outputMissing() {
        return failure(RobotDiffFailureCode.OUTPUT_MISSING,
                "ROBOT diff execution finished, but no markdown output was produced.");
    }

    private static RobotDiffFailure classifyImportFailure(UnloadableImportException importException, String combinedMessage) {
        String importIri = extract(IMPORT_IRI_PATTERN, importException.getMessage());
        String importSuffix = importIri == null ? "" : " Import IRI: " + importIri + ".";
        String httpStatus = extract(HTTP_STATUS_PATTERN, combinedMessage);

        if (httpStatus != null) {
            String rateLimitHint = "429".equals(httpStatus)
                    ? " This often happens during batch processing when many ontologies repeatedly request the same remote import."
                    : "";
            return failure(RobotDiffFailureCode.IMPORT_HTTP_ERROR,
                    "ROBOT could not load an imported ontology because the remote server returned HTTP "
                            + httpStatus + "." + importSuffix + rateLimitHint);
        }

        if (isParseMessage(combinedMessage)) {
            return failure(RobotDiffFailureCode.IMPORT_PARSE_ERROR,
                    "ROBOT could not parse an imported ontology. The import URL may have returned HTML, RDFa, or another non-OWL/RDF response instead of an ontology document."
                            + importSuffix + " Technical detail: " + firstUsefulMessage(importException, importException));
        }

        return failure(RobotDiffFailureCode.IMPORT_LOAD_ERROR,
                "ROBOT could not load an imported ontology." + importSuffix
                        + " The ontology file itself may be valid, but one of its imports was unavailable during processing."
                        + " Technical detail: " + firstUsefulMessage(importException, importException));
    }

    private static boolean isParseMessage(String message) {
        String lower = message.toLowerCase();
        return lower.contains("unparsableontologyexception")
                || lower.contains("problem parsing")
                || lower.contains("content is not allowed in prolog")
                || lower.contains("did not recognise rdf format")
                || lower.contains("text/html")
                || lower.contains("rdfa")
                || lower.contains("<!doctype html")
                || lower.contains("<!doctype html");
    }

    private static boolean isMongoDocumentSizeFailure(String message) {
        String lower = message.toLowerCase();
        return lower.contains("payload document size is larger than maximum")
                || lower.contains("bsonmaximumsizeexceededexception");
    }

    private static List<Throwable> causes(Throwable throwable) {
        List<Throwable> causes = new ArrayList<>();
        Throwable current = throwable;
        while (current != null && !causes.contains(current)) {
            causes.add(current);
            current = current.getCause();
        }
        return causes;
    }

    private static String combinedMessage(List<Throwable> causes) {
        StringBuilder builder = new StringBuilder();
        for (Throwable cause : causes) {
            builder.append(cause.getClass().getSimpleName()).append(": ");
            if (cause.getMessage() != null) {
                builder.append(cause.getMessage());
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private static <T extends Throwable> T findCause(List<Throwable> causes, Class<T> type) {
        for (Throwable cause : causes) {
            if (type.isInstance(cause)) {
                return type.cast(cause);
            }
        }
        return null;
    }

    private static String extract(Pattern pattern, String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String firstUsefulMessage(Throwable preferred, Throwable fallback) {
        String message = preferred.getMessage();
        if (message == null || message.isBlank()) {
            message = fallback.getMessage();
        }
        if (message == null || message.isBlank()) {
            return "No detailed error message was provided.";
        }
        return limit(message);
    }

    private static RobotDiffFailure failure(RobotDiffFailureCode code, String message) {
        return new RobotDiffFailure(code, limit(message));
    }

    private static String limit(String message) {
        if (message.length() <= MAX_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_MESSAGE_LENGTH) + "...";
    }
}
