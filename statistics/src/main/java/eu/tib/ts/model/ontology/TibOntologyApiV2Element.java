package eu.tib.ts.model.ontology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a single ontology element from TIB Terminology Service API v2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TibOntologyApiV2Element {
    @JsonProperty("baseUri")
    private List<String> baseUri;

    @JsonProperty("classifications")
    private List<Map<String, Object>> classifications;

    @JsonProperty("definition")
    private List<String> definition;

    @JsonProperty("definition_property")
    private List<String> definitionProperty;

    @JsonProperty("description")
    private String description;

    @JsonProperty("imported")
    private boolean imported;

    @JsonProperty("importsFrom")
    private List<String> importsFrom;

    @JsonProperty("iri")
    private String iri;

    @JsonProperty("isObsolete")
    private boolean isObsolete;

    @JsonProperty("label")
    private List<String> label;

    @JsonProperty("label_property")
    private String labelProperty;

    @JsonProperty("language")
    private List<String> language;

    @JsonProperty("license")
    private Map<String, Object> license;

    @JsonProperty("loaded")
    private String loaded;

    @JsonProperty("numberOfClasses")
    private String numberOfClasses;

    @JsonProperty("numberOfEntities")
    private String numberOfEntities;

    @JsonProperty("numberOfIndividuals")
    private String numberOfIndividuals;

    @JsonProperty("numberOfProperties")
    private String numberOfProperties;

    @JsonProperty("ontologyId")
    private String ontologyId;

    @JsonProperty("ontologyPurl")
    private String ontologyPurl;

    @JsonProperty("preferredPrefix")
    private String preferredPrefix;

    @JsonProperty("title")
    private String title;

    @JsonProperty("type")
    private List<String> type;

    /**
     * Convert this API v2 element to the internal TsOntology format
     */
    public TsOntology toTsOntology() {
        return TsOntology.builder()
                .ontologyId(this.ontologyId)
                .loaded(this.loaded)
                .numberOfTerms(parseInt(this.numberOfEntities, 0))
                .numberOfProperties(parseInt(this.numberOfProperties, 0))
                .numberOfIndividuals(parseInt(this.numberOfIndividuals, 0))
                .config(Config.builder()
                        .fileLocation(this.ontologyPurl)
                        .title(this.title)
                        .build())
                .build();
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
