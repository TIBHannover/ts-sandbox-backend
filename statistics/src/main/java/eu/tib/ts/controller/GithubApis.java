package eu.tib.ts.controller;

import eu.tib.ts.model.tags.GitFinalResponse;
import eu.tib.ts.repository.GitMediaCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GithubApis {


    @Autowired
    GitMediaCollection gitMediaCollection;

    @GetMapping("/github")
    public String login() {
        return "Welcome to login screen";
    }


    @GetMapping("/getGitMedia")
    public List<GitFinalResponse> getGitMedia() {
        List<GitFinalResponse> gitMediaCollectionAll = gitMediaCollection.findAll();
        return gitMediaCollectionAll;
    }

}

