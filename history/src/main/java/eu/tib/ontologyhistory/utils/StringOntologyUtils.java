package eu.tib.ontologyhistory.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringOntologyUtils {

    private StringOntologyUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<Boolean, List<String>> addedDeletedMap(List<String> lines) {
        return lines.stream()
                .filter(line -> (line.startsWith("+")) || (line.startsWith("-")))
                .collect(Collectors.groupingBy(line -> Pattern.compile("\\+ ").matcher(line).find()));
    }

    public static List<String> editedLines(Map<Boolean, List<String>> booleanListMap) {
        return Optional.ofNullable(booleanListMap.get(false)).orElse(Collections.emptyList())
                .stream()
                .filter(line -> {
                            if (!line.contains("<")) {
                                return false;
                            }
                            return Optional.ofNullable(booleanListMap.get(true))
                            .orElse(Collections.emptyList())
                            .toString().contains(line.substring(line.indexOf("<"), line.indexOf(">") + 1));
                        })
                .toList();
    }
}
