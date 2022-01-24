package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class CharacteristicsInfo {
    long size;
    List<String> list;

    public static CharacteristicsInfo of(List<String> similarities) {
        return CharacteristicsInfo.builder()
            .size(similarities.size())
            .list(similarities)
            .build();
    }
}
