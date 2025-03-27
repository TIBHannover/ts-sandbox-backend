package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.service.network.GithubService;
import eu.tib.ontologyhistory.service.network.GitlabService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class GitServiceFactory {

    private static final Map<String, String> gitServiceTypes = new HashMap<>(Map.ofEntries(
            Map.entry("gitlab.com", "gitlab"),
            Map.entry("git.tib.eu", "gitlab"),

            Map.entry("raw.githubusercontent.com", "github")

    ));

    private GitServiceFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static GitService<? extends Commit> getService(URI uri) {
        String host = uri.getHost();
        String serviceType = gitServiceTypes.get(host);

        if (serviceType == null) {
            throw new IllegalArgumentException("Unsupported Git service: " + host);
        }

        return createGitService(serviceType);
    }

    private static GitService<? extends Commit> createGitService(String serviceType) {
        return switch (serviceType) {
            case "gitlab" -> new GitlabService();
            case "github" -> new GithubService();
            default -> throw new IllegalArgumentException("Unsupported Git service type: " + serviceType);
        };
    }

    public static boolean isHostSupported(URI uri) {
        return gitServiceTypes.containsKey(uri.getHost());
    }

}
