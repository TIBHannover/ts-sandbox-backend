package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

@Builder
@Value
public class PairwiseSimilarity {
    Pair<String, String> pair;
    double sum;
    double totalSum;
    double percent;
    Pair<String, String> titles;
    Map<String, CharacteristicsInfo> characteristics;
}
