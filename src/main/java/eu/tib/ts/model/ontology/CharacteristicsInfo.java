package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.util.Pair;

import java.util.List;

@Builder
@Value
public class CharacteristicsInfo {
    long size;
    long maxSimilaritiesSize;
    double percent;
    List<Pair<String, String>> list;

    public static CharacteristicsInfo of(List<Pair<String, String>> similarities, long maxSimilaritiesSize, double percent) {
        return CharacteristicsInfo.builder()
                .size(similarities.size())
                .maxSimilaritiesSize(maxSimilaritiesSize)
                .percent(percent)
                .list(similarities)
                .build();
    }
}

