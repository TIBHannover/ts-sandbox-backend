package eu.tib.ts.model.ontology;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class Objects {
    String classUri;
    String classLabel;
}
