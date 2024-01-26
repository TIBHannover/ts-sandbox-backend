package eu.tib.tiva.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Configuration
@ConfigurationProperties(prefix = "skip")
public class TivaProcessingConfig {
    private List<String> tivaList = new ArrayList<>();

    public TivaProcessingConfig() {
    }

    public List<String> getTivaList() {
        return tivaList;
    }

    public void setTivaList(List<String> tivaList) {
        this.tivaList = tivaList;
    }
}
