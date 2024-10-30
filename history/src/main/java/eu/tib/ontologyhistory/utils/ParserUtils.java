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

    private static Axiom getAxiomFromDiffItem(String diffLine) {
        diffLine = diffLine.substring(1).trim();

        int startIndex = diffLine.indexOf("(");
        int lastIndex = diffLine.lastIndexOf(")");

        int lessThanIndex = diffLine.indexOf("<");
        int greaterThanIndex = diffLine.indexOf(">");

        if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
            String axiomType = diffLine.substring(0, startIndex).trim();
            String axiomValue = diffLine.substring(startIndex + 1, lastIndex).trim();
            String axiomURI = "";
            if (lessThanIndex != -1 && greaterThanIndex != -1 && lessThanIndex < greaterThanIndex) {
                axiomURI = diffLine.substring(lessThanIndex, greaterThanIndex + 1).trim();
            }

            return new Axiom(axiomType, axiomValue, axiomURI);

        }

        return null;
    }

    public static Map<String, List<Axiom>> parseAxioms(List<String> diff) {
        Map<String, List<Axiom>> axioms = new HashMap<>();
        axioms.put("added", new ArrayList<>());
        axioms.put("removed", new ArrayList<>());

        for (String line : diff) {

            boolean isAdded = line.startsWith("+");

            Axiom axiom = getAxiomFromDiffItem(line);

            if (axiom != null) {
                if (isAdded) {
                    axioms.get("added").add(axiom);
                } else {
                    axioms.get("removed").add(axiom);
                }
            }
        }

        return axioms;
    }
}
