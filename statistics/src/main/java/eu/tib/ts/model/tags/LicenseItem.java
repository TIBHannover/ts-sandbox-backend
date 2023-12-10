package eu.tib.ts.model.tags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LicenseItem {
    public String name;
    public String path;
    public String sha;
    public int size;
    public String url;
    public String html_url;
    public String git_url;
    public String download_url;
    public String type;
    public String content;
    public String encoding;
    public Links _links;
    public License license;
}
