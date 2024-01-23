package eu.tib.tiva.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TsTiva {

    String tivaId;

}
