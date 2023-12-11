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

            if (gitRepos.size() > 0) {
                gitRepository.deleteAll();
            }

            tsOntologies.stream().filter(ts -> !ts.getConfig().getRepoUrl().equals("") && ts.getConfig().getRepoUrl().contains("github.com")).forEach(watchItem -> {
                gitRepository.save(GitRepo.builder().ontologyId(watchItem.getOntologyId()).title(watchItem.getTitle()).repoUrl(watchItem.getConfig().getRepoUrl()).repositoryName(watchItem.getConfig().getRepoUrl().substring(18)).build());
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


            System.out.println("maxWatches :  " + maxWatches + "  maxForks :  " + maxForks);

            float watchesVal = 0.0f;
            float forksVal = 0.0f;
            float starsVal = 0.0f;
            ArrayList<Float> valEstimationArr = new ArrayList<Float>();

            float valEst = 0.0f;
            int count = 0;


            for (GitFinalResponse gitFinalRespons : gitFinalResponses) {
                //Compare elements of array with max
                if (gitFinalRespons.getWatches() > maxWatches) {
                    maxWatches = gitFinalRespons.getWatches();
                    if (maxWatches == gitFinalResponses.get(count).getWatches()) {
                        watchesVal = 33.33f;
                    }
                } else {
                    watchesVal = 0.0f;
                }
                if (gitFinalRespons.getLikes() > maxStars) {
                    maxStars = gitFinalRespons.getLikes();
                    if (maxStars == gitFinalResponses.get(count).getLikes()) {
                        starsVal = 33.33f;
                    }
                } else {
                    starsVal = 0.0f;
                }
                if (gitFinalRespons.getForks() > maxForks) {
                    maxForks = gitFinalRespons.getForks();
                    if (maxForks == gitFinalResponses.get(count).getForks()) {
                        forksVal = 33.33f;
                    }
                } else {
                    forksVal = 0.0f;
                }

                valEst = watchesVal + forksVal + starsVal;

                valEstimationArr.add(valEst);
                count++;
                System.out.println("valEst :  " + valEst + "  counter :  " + count);

                gitMediaCollection.save(GitFinalResponse.builder().ontologyId(gitFinalRespons.getOntologyId()).title(gitFinalRespons.getTitle()).repoUrl(gitFinalRespons.getRepoUrl()).forks(gitFinalRespons.getForks()).watches(gitFinalRespons.getWatches()).likes(gitFinalRespons.getLikes()).releases(gitFinalRespons.getReleases()).readMe(gitFinalRespons.getReadMe()).license(gitFinalRespons.getLicense()).booleanEstimation(gitFinalRespons.getBooleanEstimation()).valuesEstimation(valEst).build());

            }

            System.out.println("Titled : after loop");

        }
    }

}
