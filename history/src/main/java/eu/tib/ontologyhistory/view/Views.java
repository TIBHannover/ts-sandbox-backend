package eu.tib.ontologyhistory.view;

public class Views {

    public static interface Edit {}

    public static interface Update {}

    public static interface Swagger {}

    public static interface Full extends Edit, Update, Swagger {}
}
