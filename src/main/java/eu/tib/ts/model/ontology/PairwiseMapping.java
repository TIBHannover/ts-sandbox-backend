package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;

import org.springframework.data.util.Pair;

@Builder
@Value
public class PairwiseMapping {

    Pair<String, String> pair;
    String mappingSourceUri;
    String mappingTargetUri;

}