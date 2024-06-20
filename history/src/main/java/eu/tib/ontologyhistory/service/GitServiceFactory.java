package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.service.network.GithubService;
import eu.tib.ontologyhistory.service.network.GitlabService;
import lombok.val;

import java.net.URI;

public class GitServiceFactory {

    public static GitService<?> getService(String link) {
        val uri = URI.create(link);

        String host = uri.getHost();

        if (host.contains("gitlab")) {
            return new GitlabService();
        } else if (host.contains("github")) {
            return new GithubService();
        } else {
            throw new IllegalArgumentException("Unsupported Git service: " + host);
        }
    }
}
