package eu.tib.ts.assessments.model.tags.ontology;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class Classification {
    List<String> collection;
    List<String> dfg;
}
