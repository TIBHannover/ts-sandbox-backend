package eu.tib.ontologyhistory.utils;

import eu.tib.ontologyhistory.model.Axiom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserUtils {

    private ParserUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, List<Axiom>> parseAxioms(List<String> diff) {
        Map<String, List<Axiom>> axioms = new HashMap<>();
        axioms.put("added", new ArrayList<>());
        axioms.put("removed", new ArrayList<>());

        for (String line : diff) {

            boolean isAdded = line.startsWith("+");
            boolean isRemoved = line.startsWith("-");

            if (isAdded || isRemoved) {
                line = line.substring(1).trim();

                int startIndex = line.indexOf("(");
                int lastIndex = line.lastIndexOf(")");

                int lessThanIndex = line.indexOf("<");
                int greaterThanIndex = line.indexOf(">");
                if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
                    String axiomType = line.substring(0, startIndex).trim();
                    String axiomValue = line.substring(startIndex + 1, lastIndex).trim();
                    String axiomURI = "";
                    if (lessThanIndex != -1 && greaterThanIndex != -1 && lessThanIndex < greaterThanIndex) {
                        axiomURI = line.substring(lessThanIndex, greaterThanIndex + 1).trim();
                    }

                    Axiom axiom = new Axiom(axiomType, axiomValue, axiomURI);

                    if (isAdded) {
                        axioms.get("added").add(axiom);
                    } else {
                        axioms.get("removed").add(axiom);
                    }
                }
            }
        }

        return axioms;
    }
}
