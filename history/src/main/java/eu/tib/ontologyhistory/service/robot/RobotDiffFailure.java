package eu.tib.ontologyhistory.service.robot;

public record RobotDiffFailure(
        RobotDiffFailureCode code,
        String message
) {
}
