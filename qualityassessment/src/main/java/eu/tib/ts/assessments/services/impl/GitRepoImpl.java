package eu.tib.ts.assessments.services.impl;

import eu.tib.ts.assessments.model.tags.*;
import eu.tib.ts.assessments.repository.QualityAssessment;
import eu.tib.ts.assessments.repository.GitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Repository
public class GitRepoImpl {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com/repos";
    private static final float METRIC_WEIGHT = 33.33f;
    private final String GITHUB_ACCESS_TOKEN = "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";

    @Autowired
    private GitRepository gitRepository;

    @Autowired
    private QualityAssessment qualityAssessment;
    private final RestTemplate restTemplate;
    private final ArrayList<GitFinalResponse> gitFinalResponsesArray = new ArrayList<>();

    @Autowired
    public GitRepoImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<GitFinalResponse> getOntologies() {
        resetAssessments();

        List<GitRepo> gitRepos = gitRepository.findAll();

        for (GitRepo repo : gitRepos) {
            processRepository(repo);
        }

        return new ArrayList<>(gitFinalResponsesArray);
    }


    private void resetAssessments() {
        if (!qualityAssessment.findAll().isEmpty()) {
            qualityAssessment.deleteAll();
        }
    }

    private void processRepository(GitRepo repo) {
        String repositoryName = repo.getRepositoryName();

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        GitHubRepository gitHubRepository = fetchRepositoryData(GITHUB_API_BASE_URL + repositoryName, entity);

        int watchCount = gitHubRepository != null ? gitHubRepository.getSubscribersCount() : 0;
        int starCount = gitHubRepository != null ? gitHubRepository.getStargazersCount() : 0;
        int forkCount = gitHubRepository != null ? gitHubRepository.getForksCount() : 0;

        boolean hasReleases = checkIfExists(GITHUB_API_BASE_URL + repositoryName + "/releases", entity, new ParameterizedTypeReference<List<Releases>>() {});
        boolean hasReadMe = checkIfExists(GITHUB_API_BASE_URL + repositoryName + "/readme", entity, new ParameterizedTypeReference<ReadMeItem>() {});
        boolean hasLicense = checkIfExists(GITHUB_API_BASE_URL + repositoryName + "/license", entity, new ParameterizedTypeReference<LicenseItem>() {});

        float estimatedValue = calculateDataAssessmentScore(hasReleases, hasReadMe, hasLicense);

        gitFinalResponsesArray.add(
                GitFinalResponse.builder()
                        .ontologyId(repo.getOntologyId())
                        .title(repo.getTitle())
                        .repoUrl(repo.getRepoUrl())
                        .forks(forkCount)
                        .watchers(watchCount)
                        .stars(starCount)
                        .hasReleases(hasReleases)
                        .hasReadMe(hasReadMe)
                        .hasLicense(hasLicense)
                        .dataAssessmentScore(estimatedValue)
                        .build()
        );
    }

    private GitHubRepository fetchRepositoryData(String url, HttpEntity<String> entity) {
        try {
            ResponseEntity<GitHubRepository> response = restTemplate.exchange(url, HttpMethod.GET, entity, GitHubRepository.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(GITHUB_ACCESS_TOKEN);
        return headers;
    }

    private <T> int getItemCount(String url, HttpEntity<String> entity, ParameterizedTypeReference<List<T>> responseType) {
        try {
            ResponseEntity<List<T>> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            return response.getBody() != null ? response.getBody().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private <T> boolean checkIfExists(String url, HttpEntity<String> entity, ParameterizedTypeReference<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            return response.hasBody();
        } catch (Exception e) {
            return false;
        }
    }

    private float calculateDataAssessmentScore(boolean hasReleases, boolean hasReadMe, boolean hasLicense) {
        float releaseVal = hasReleases ? METRIC_WEIGHT : 0.0f;
        float readMeVal = hasReadMe ? METRIC_WEIGHT : 0.0f;
        float licenseVal = hasLicense ? METRIC_WEIGHT : 0.0f;
        return releaseVal + readMeVal + licenseVal;
    }

}