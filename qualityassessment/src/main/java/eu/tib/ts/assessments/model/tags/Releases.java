package eu.tib.ts.assessments.model.tags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.Date;



@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Releases {

    public String url;
    public String assets_url;
    public String upload_url;
    public String html_url;
    public int id;
    public Author author;
    public String node_id;
    public String tag_name;
    public String target_commitish;
    public String name;
    public boolean draft;
    public boolean prerelease;
    public Date created_at;
    public Date published_at;
    public ArrayList<Object> assets;
    public String tarball_url;
    public String zipball_url;
    public String body;
    public int mentions_count;

}
