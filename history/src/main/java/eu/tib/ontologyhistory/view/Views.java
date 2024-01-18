package eu.tib.ontologyhistory.view;

public class Views {

    public static interface Edit {}

    public static interface Update {}

    public static interface Short {}

    public static interface Add extends Edit {}

    public static interface Full extends Short, Update, Add {}
}
