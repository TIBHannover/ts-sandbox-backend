package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class MappingCharacteristicsInfo {

    long size;
    List<String> mappingsList;

    public static MappingCharacteristicsInfo of (long size, List<String> mappingsList){

        return MappingCharacteristicsInfo.builder()
                .size(size)
                .mappingsList(mappingsList)
                .build();
    }
}