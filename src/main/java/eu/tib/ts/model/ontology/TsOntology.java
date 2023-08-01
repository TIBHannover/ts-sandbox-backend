package eu.tib.ts.model.ontology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TsOntology implements Ontology {
    String ontologyId;
    String loaded;
    String updated;
    String status;
    String message;
    Object version;
    String fileHash;
    int loadAttempts;
    int numberOfTerms;
    int numberOfProperties;
    int numberOfIndividuals;
    Config config;

    public Set<String> getCollection() {
        if (config == null || CollectionUtils.isEmpty(config.getClassifications())) {
            return Collections.emptySet();
        } else {
            Classification classification = config.getClassifications().get(0);
            return classification != null && classification.getCollection() != null
                    ? new HashSet<>(classification.getCollection())
                    : Collections.emptySet();
        }
    }

    public String getUri() {
        return config != null ? config.getFileLocation() : null;
    }

    @Override
    public String getTitle() {
        return Objects.isNull(config)
                ? null
                : config.getTitle();
    }
}