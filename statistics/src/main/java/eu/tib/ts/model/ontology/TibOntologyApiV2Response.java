package eu.tib.ts.model.ontology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the paginated response from TIB Terminology Service API v2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TibOntologyApiV2Response {
    @JsonProperty("page")
    private int page;

    @JsonProperty("numElements")
    private int numElements;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("totalElements")
    private int totalElements;

    @JsonProperty("elements")
    private List<TibOntologyApiV2Element> elements;
}
