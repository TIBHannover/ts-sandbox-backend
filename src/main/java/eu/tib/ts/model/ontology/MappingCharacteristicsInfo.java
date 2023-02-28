package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class MappingCharacteristicsInfo {

    long size;
    List<String> mappingsList;

}