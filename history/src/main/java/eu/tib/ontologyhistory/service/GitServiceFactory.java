package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.service.network.GithubService;
import eu.tib.ontologyhistory.service.network.GitlabService;

import java.net.URI;

public class GitServiceFactory {

    private GitServiceFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static GitService<? extends Commit> getService(URI uri) {

        String host = uri.getHost();

        if (host.equals("gitlab.com")) {
            return new GitlabService();
        } else if (host.equals("raw.githubusercontent.com")) {
            return new GithubService();
        } else {
            throw new IllegalArgumentException("Unsupported Git service: " + host);
        }
    }
}
