package eu.tib.ontologyhistory.service;

import eu.tib.ontologyhistory.model.Commit;
import eu.tib.ontologyhistory.service.network.GitService;
import eu.tib.ontologyhistory.service.network.GithubService;
import eu.tib.ontologyhistory.service.network.GitlabService;
import lombok.Getter;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public enum GitServiceType {
    GITLAB("gitlab.com") {
        @Override
        public GitService<? extends Commit> createService() {
            return new GitlabService(GitTokenType.GITLAB_TOKEN, GitServiceType.GITLAB.getHost());
        }
    },
    GIT_AACHEN("git.rwth-aachen.de") {
        @Override
        public GitService<? extends Commit> createService() {
            return new GitlabService(GitTokenType.GIT_AACHEN_TOKEN, GitServiceType.GIT_AACHEN.getHost());
        }
    },
    GIT_TIB_EU("git.tib.eu") {
        @Override
        public GitService<? extends Commit> createService() {
            return new GitlabService(GitTokenType.GIT_TIB_EU_TOKEN, GitServiceType.GIT_TIB_EU.getHost());
        }
    },
    GITHUB("raw.githubusercontent.com") {
        @Override
        public GitService<? extends Commit> createService() {
            return new GithubService(GitTokenType.GITHUB_TOKEN, GitServiceType.GITHUB.getHost());
        }
    };

    private final String host;

    GitServiceType(String host) {
        this.host = host;
    }

    private static final Map<String, GitServiceType> GIT_SERVICES;

    static {
        Map<String, GitServiceType> map = new ConcurrentHashMap<>();
        for (GitServiceType gitServiceType : GitServiceType.values()) {
            map.put(gitServiceType.host, gitServiceType);
        }
        GIT_SERVICES = Collections.unmodifiableMap(map);
    }

    public static GitServiceType getService(String host) {
        return GIT_SERVICES.get(host);
    }

    public abstract GitService<? extends Commit> createService();

    public static boolean isHostSupported(URI uri) {
        return GIT_SERVICES.containsKey(uri.getHost());
    }

    public static GitService<? extends Commit> createService(URI uri) throws IllegalArgumentException{
        GitServiceType type = getService(uri.getHost());
        if (type == null) {
            throw new IllegalArgumentException("Unknown host: " + uri);
        }
        return type.createService();
    }
}
