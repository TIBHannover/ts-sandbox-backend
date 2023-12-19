package eu.tib.ts.service.impl;

import eu.tib.ts.controller.GithubApis;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.model.tags.GitFinalResponse;
import eu.tib.ts.model.tags.GitRepo;
import eu.tib.ts.repository.GitMediaCollection;
import eu.tib.ts.repository.GitRepository;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.GitPreProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class GitTagPreProcessingServiceImpl implements GitPreProcessingService {

    private final TsRepository tsRepository;
    private final GithubApis githubApis;
    private final GitRepository gitRepository;

    private final GitMediaCollection gitMediaCollection;

    private GitRepoImpl gitRepoImp;

    @Autowired
    public GitTagPreProcessingServiceImpl(TsRepository tsRepository, GithubApis githubApis, GitRepository gitRepository,  GitRepoImpl gitRepoImp, GitMediaCollection gitMediaCollection) {
        this.tsRepository = tsRepository;
        this.gitRepository = gitRepository;
        this.githubApis = githubApis;
        this.gitRepoImp = gitRepoImp;
        this.gitMediaCollection = gitMediaCollection;
    }


    @Override
    public void doGitPreProcessing() {
        try {

            List<TsOntology> tsOntologies = tsRepository.getOntologies();

            List<GitRepo> gitRepos = gitRepository.findAll();

            System.out.println("GitRepos " + gitRepos);
            if (gitRepos.size() > 0) {
                gitRepository.deleteAll();
            }

            tsOntologies.stream().filter(ts -> !ts.getConfig().getRepoUrl().equals("") && ts.getConfig().getRepoUrl().contains("github.com")).forEach(watchItem -> {

                String repoUrl = watchItem.getConfig().getRepoUrl();

                String extractRepoName = extractRepositoryPath(repoUrl);

                gitRepository.save(GitRepo.builder().ontologyId(watchItem.getOntologyId()).title(watchItem.getTitle()).repoUrl(repoUrl).repositoryName(extractRepoName).build());


                System.out.println("single processing : ontology_id " + watchItem.getOntologyId() + "\n title : " + watchItem.getTitle() + "\n repo_url : " + watchItem.getConfig().getRepoUrl());
            });

        } catch (Exception e) {
            System.out.println("Titled doPreProcessing: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            List<GitFinalResponse> gitFinalResponses = gitRepoImp.getOntologies();

            int maxWatches = gitFinalResponses.get(0).getWatches();
            int maxForks = gitFinalResponses.get(0).getForks();
            int maxStars = gitFinalResponses.get(0).getLikes();

            System.out.println("maxWatches first value:  " + maxWatches );

            float watchesVal = 0.0f;
            float forksVal = 0.0f;
            float starsVal = 0.0f;
            ArrayList<Float> valEstimationArr = new ArrayList<Float>();

            float valEst = 0.0f;
            int count = 0;

            for (GitFinalResponse gitFinalRespons : gitFinalResponses) {

                if (gitFinalRespons.getWatches() > maxWatches) {
                    maxWatches = gitFinalRespons.getWatches();
                }

                if (gitFinalRespons.getLikes() > maxStars) {
                    maxStars = gitFinalRespons.getLikes();
                }

                if (gitFinalRespons.getForks() > maxForks) {
                    maxForks = gitFinalRespons.getForks();
                }
                System.out.println("executing first loop");

                count++;

            }

            System.out.println("maxwatch :  " + maxWatches + "  maxstart :  " + maxStars + "  forksVal :  " + maxForks );

            for (GitFinalResponse gitFinalRespons : gitFinalResponses) {

                System.out.println("executing second loop");

                watchesVal = (float) ((gitFinalRespons.getWatches() * 33.33)/maxWatches);

                starsVal = (float) ((gitFinalRespons.getLikes() * 33.33)/maxStars);

                forksVal = (float) ((gitFinalRespons.getForks() * 33.33)/maxForks);

                valEst = watchesVal + forksVal + starsVal;

                valEstimationArr.add(valEst);
                count++;
                System.out.println("valEst :  " + valEst + "  counter :  " + count);

                gitMediaCollection.save(GitFinalResponse.builder().ontologyId(gitFinalRespons.getOntologyId()).title(gitFinalRespons.getTitle()).repoUrl(gitFinalRespons.getRepoUrl()).forks(gitFinalRespons.getForks()).watches(gitFinalRespons.getWatches()).likes(gitFinalRespons.getLikes()).releases(gitFinalRespons.getReleases()).readMe(gitFinalRespons.getReadMe()).license(gitFinalRespons.getLicense()).booleanEstimation(gitFinalRespons.getBooleanEstimation()).valuesEstimation(valEst).build());

            }

            System.out.println("Titled : after loop");

        }
    }

    private static String extractRepositoryPath(String url) {
        // Find the index after "github.com/"
        int startIndex = url.indexOf("github.com/") + "github.com/".length();

        // Find the index of the next slash '/'
        int firstSlashIndex = url.indexOf('/', startIndex);

        // If the first slash is found, find the index of the next slash
        if (firstSlashIndex != -1) {
            int secondSlashIndex = url.indexOf('/', firstSlashIndex + 1);

            // Extract the repository path until the second slash
            if (secondSlashIndex != -1) {
                return url.substring(startIndex, secondSlashIndex);
            } else {
                // If there is no second slash, extract the path until the end of the URL
                return url.substring(startIndex);
            }
        }

        return null; // Handle the case where the format is unexpected
    }

}
