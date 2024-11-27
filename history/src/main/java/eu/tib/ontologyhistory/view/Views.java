package eu.tib.ontologyhistory.view;

public class Views {

    private Views() {
        throw new IllegalStateException("Utility class");
    }

    public interface Edit {}

    public interface Update {}

    public interface Short {}

    public interface Add extends Edit {}

    public interface Full extends Short, Update, Add {}
}
