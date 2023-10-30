package eu.tib.ts.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "skip")
public class OntologiesProcessingConfig {
    private List<String> ontologies = new ArrayList<>();

    public OntologiesProcessingConfig() {
    }

    public List<String> getOntologies() {
        return ontologies;
    }

    public void setOntologies(List<String> ontologies) {
        this.ontologies = ontologies;
    }
}
