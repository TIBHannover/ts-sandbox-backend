package eu.tib.ts.model.ontology;

import lombok.Builder;
import lombok.Value;

import org.springframework.data.util.Pair;

import java.util.Map;

@Builder
@Value
public class PairwiseMapping {

    Pair<String, String> pair;

    //the total number of mappings between two ontologies withing one collection.
    double sum;

    //the titles of ontologies for which we calculate mappings.
    Pair<String, String> titles;

    //map of all mapping between ontologies
    Map<String, MappingCharacteristicsInfo> characteristics;
}