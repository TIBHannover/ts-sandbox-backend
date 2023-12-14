package eu.tib.ts.model.tags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "git_repos_doc")
public class GitRepo {


    String ontologyId;
    String title;
    String repoUrl;
    String repositoryName;


}

