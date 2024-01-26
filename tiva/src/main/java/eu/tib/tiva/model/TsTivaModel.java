package eu.tib.tiva.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TsTivaModel {

    String tivaId;
    List<String> tivaCountryCode;

}
