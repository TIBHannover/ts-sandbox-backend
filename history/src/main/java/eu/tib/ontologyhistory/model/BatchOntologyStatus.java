package eu.tib.ontologyhistory.model;

public enum BatchOntologyStatus {
    PROCESSING,
    ADDED,
    ADDED_WITH_WARNINGS,
    NO_COMPARABLE_VERSIONS,
    UNSUPPORTED_HOST,
    FAILED
}
