package eu.tib.ts.assessments.services.impl;


import eu.tib.ts.assessments.controller.AssessmentController;
import eu.tib.ts.assessments.model.tags.GitFinalResponse;
import eu.tib.ts.assessments.model.tags.QualityAssessmentDetails;
import eu.tib.ts.assessments.model.tags.GitRepo;
import eu.tib.ts.assessments.model.tags.ontology.TsOntology;
import eu.tib.ts.assessments.services.GitPreProcessingService;
import eu.tib.ts.assessments.repository.QualityAssessment;
import eu.tib.ts.assessments.repository.GitRepository;
import eu.tib.ts.assessments.repository.TsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class GitTagPreProcessingServiceImpl implements GitPreProcessingService {

    private final TsRepository tsRepository;
    private final AssessmentController assessmentController;
    private final GitRepository gitRepository;
    private final QualityAssessment qualityAssessment;
    private final GitRepoImpl gitRepoImp;

    @Autowired
    public GitTagPreProcessingServiceImpl(
            TsRepository tsRepository,
            AssessmentController assessmentController,
            GitRepository gitRepository,
            GitRepoImpl gitRepoImp,
            QualityAssessment qualityAssessment
    ) {
        this.tsRepository = tsRepository;
        this.assessmentController = assessmentController;
        this.gitRepository = gitRepository;
        this.gitRepoImp = gitRepoImp;
        this.qualityAssessment = qualityAssessment;
        log.info("GitTagPreProcessingService initialized");
    }

    @Override
    public void performGitPreProcessing() {
        try {
            // Fetch ontologies and existing Git repositories
            List<TsOntology> ontologies = tsRepository.getOntologies();
            List<GitRepo> existingRepos = gitRepository.findAll();

            log.info("Fetched ontologies: {}", ontologies);

            // Clear existing Git repositories
            if (!existingRepos.isEmpty()) {
                gitRepository.deleteAll();
                log.info("Cleared existing Git repositories");
            }

            // Process and save repositories from ontologies
            ontologies.stream()
                    .filter(ontology -> isValidGitHubUrl(ontology.getConfig().getRepoUrl()))
                    .forEach(this::saveRepositoryFromOntology);
        } catch (Exception e) {
            log.error("Error during Git pre-processing: {}", e.getMessage(), e);
        } finally {
            processRepositoryMetrics();
        }
    }

    private boolean isValidGitHubUrl(String url) {
        return url != null && !url.isEmpty() && url.contains("github.com");
    }

    private void saveRepositoryFromOntology(TsOntology ontology) {
        String repoUrl = ontology.getConfig().getRepoUrl();
        String githubPath = extractGitHubPath(repoUrl);

        if (!"invalid".equals(githubPath)) {
            GitRepo gitRepo = GitRepo.builder()
                    .ontologyId(ontology.getOntologyId())
                    .title(ontology.getTitle())
                    .repoUrl(repoUrl)
                    .repositoryName(githubPath)
                    .build();
            gitRepository.save(gitRepo);
            log.info("Saved repository: {}", gitRepo);
        } else {
            log.warn("Invalid repository URL: {}", repoUrl);
        }
    }

    private void processRepositoryMetrics() {
        List<GitFinalResponse> gitFinalResponses = gitRepoImp.getOntologies();

        if (gitFinalResponses.isEmpty()) {
            log.warn("No Git repositories found to process metrics");
            return;
        }

        // Calculate maximum metrics
        int maxWatches = gitFinalResponses.stream().mapToInt(GitFinalResponse::getWatchers).max().orElse(1);
        int maxForks = gitFinalResponses.stream().mapToInt(GitFinalResponse::getForks).max().orElse(1);
        int maxStars = gitFinalResponses.stream().mapToInt(GitFinalResponse::getStars).max().orElse(1);

        log.info("Max values - Watches: {}, Forks: {}, Stars: {}", maxWatches, maxForks, maxStars);

        // Process each repository's metrics
        gitFinalResponses.forEach(response -> {
            float watchesScore = calculateNormalizedScore(response.getWatchers(), maxWatches);
            float forksScore = calculateNormalizedScore(response.getForks(), maxForks);
            float starsScore = calculateNormalizedScore(response.getStars(), maxStars);

            float communityScore = watchesScore + forksScore + starsScore;

            saveQualityAssessment(response, communityScore);
        });

        log.info("Repository metrics processing completed");
    }

    private float calculateNormalizedScore(int value, int maxValue) {
        return (value * 33.33f) / maxValue;
    }

    private void saveQualityAssessment(GitFinalResponse response, float communityScore) {
        QualityAssessmentDetails communityAssessment = QualityAssessmentDetails.builder()
                .ontologyId(response.getOntologyId())
                .title(response.getTitle())
                .repoUrl(response.getRepoUrl())
                .hasReleases(response.getHasReleases())
                .hasReadMe(response.getHasReadMe())
                .hasLicense(response.getHasLicense())
                .forks(response.getForks())
                .watchers(response.getWatchers())
                .stars(response.getStars())
                .dataAssessmentScore(response.getDataAssessmentScore())
                .communityAssessmentScore(communityScore)
                .build();

        this.qualityAssessment.save(communityAssessment);
        log.info("Saved Community Assessment: {}", communityAssessment);
    }

    private String extractGitHubPath(String url) {
        try {
            String[] parts = url.replace("https://github.com/", "").split("/");
            if (parts.length >= 2) {
                return "/" + parts[0] + "/" + parts[1];
            }
        } catch (Exception e) {
            log.error("Error extracting repository name from URL: {}", url, e);
        }
        return "invalid";
    }

}