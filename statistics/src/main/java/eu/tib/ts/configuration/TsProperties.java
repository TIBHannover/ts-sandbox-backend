package eu.tib.ts.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "ts")
@Data
public class TsProperties {
    private String baseUri = "https://api.terminology.tib.eu/api/v2/ontologies";
    private Integer ontologiesListSize = 10;
    private List<String> classifications = new ArrayList<>();
    private Timeout timeout = new Timeout();

    @Data
    public static class Timeout {
        private Integer connect = 10000;
        private Integer read = 10000;
    }
}
