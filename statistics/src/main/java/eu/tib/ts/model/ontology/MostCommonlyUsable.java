package eu.tib.ts.model.ontology;

public interface MostCommonlyUsable {
    default boolean consideredForCommonlyUsed() {
        return true;
    }
}
