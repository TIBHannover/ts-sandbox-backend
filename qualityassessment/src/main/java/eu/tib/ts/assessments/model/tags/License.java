package eu.tib.ts.assessments.model.tags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class License {
    public String key;
    public String name;
    public String spdx_id;
    public String url;
    public String node_id;
}