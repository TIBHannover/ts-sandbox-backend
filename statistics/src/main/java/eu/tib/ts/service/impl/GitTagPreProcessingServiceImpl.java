package eu.tib.ts.service.impl;

import eu.tib.ts.controller.GithubApis;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.model.tags.GitRepo;
import eu.tib.ts.repository.GitRepository;
import eu.tib.ts.repository.TsRepository;
import eu.tib.ts.service.GitPreProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class GitTagPreProcessingServiceImpl implements GitPreProcessingService {

    private final TsRepository tsRepository;
    private final GithubApis githubApis;
    private final GitRepository gitRepository;

    private GitRepoImpl gitRepoImp;

    @Autowired
    public GitTagPreProcessingServiceImpl(TsRepository tsRepository, GithubApis githubApis, GitRepository gitRepository,  GitRepoImpl gitRepoImp) {
        this.tsRepository = tsRepository;
        this.gitRepository = gitRepository;
        this.githubApis = githubApis;
        this.gitRepoImp = gitRepoImp;
    }


    @Override
    public void doGitPreProcessing() {
        try {

            List<TsOntology> tsOntologies = tsRepository.getOntologies();

            List<GitRepo> gitRepos = gitRepository.findAll();

            if(gitRepos.size()>0){
                gitRepository.deleteAll();
            }

            tsOntologies.stream().filter(ts -> !ts.getConfig().getRepoUrl().equals("") && ts.getConfig().getRepoUrl().contains("github.com")).forEach(watchItem -> {
                gitRepository.save(GitRepo.builder().ontologyId(watchItem.getOntologyId()).title(watchItem.getTitle()).repoUrl(watchItem.getConfig().getRepoUrl()).repositoryName(watchItem.getConfig().getRepoUrl().substring(18)).build());
                System.out.println("single processing : ontology_id " + watchItem.getOntologyId() +"\n title : "+watchItem.getTitle() + "\n repo_url : "+watchItem.getConfig().getRepoUrl());
            });

        } catch (Exception e) {
            System.out.println("Titled doPreProcessing: " + e.getMessage());
            throw new RuntimeException(e);
        }finally {
            gitRepoImp.getOntologies();
        }

        System.out.println("Titled : after loop");

    }

}

