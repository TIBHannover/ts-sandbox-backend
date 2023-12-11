package eu.tib.ts.service.impl;



import eu.tib.ts.model.tags.*;
import eu.tib.ts.repository.GitMediaCollection;
import eu.tib.ts.repository.GitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Repository
public class GitRepoImpl {

    private static final String QUERY_PARAM_SIZE = "size";

    @Autowired
    GitRepository gitRepository;

    private ArrayList<GitFinalResponse> gitFinalResponsesArray = new ArrayList<>();


    @Autowired
    GitMediaCollection gitMediaCollection;

    private final RestTemplate restTemplate;

    @Autowired
    public GitRepoImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public List<GitFinalResponse> getOntologies() {


        List<GitRepo> gitRepos = gitRepository.findAll();
        boolean haveReadMe = false;
        boolean haveReleases = false;
        boolean haveLicense = false;

        float readMeVal = 33.33f;
        float releaseVal = 33.33f;
        float licenseVal = 33.33f;
        float estimatedValue;

        List<WatchItem> body = new ArrayList<>();

        List<GitFinalResponse> gitFinalResponses = gitMediaCollection.findAll();
        if (gitFinalResponses.size() > 0) {
            gitMediaCollection.deleteAll();
        }

        for (GitRepo tsOntology : gitRepos) {

            String watchesUrl = "https://api.github.com/repos" + tsOntology.getRepositoryName() + "/subscribers";


            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("Github_Token");
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
//            ResponseEntity<String> response = restTemplate.exchange(watchesUrl, HttpMethod.GET, entity, String.class);
            // process response


            ResponseEntity<List<WatchItem>> whatches;
            ResponseEntity<LicenseItem> license;
            ResponseEntity<List<Releases>> releases = null;

            whatches = restTemplate.exchange(watchesUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<WatchItem>>() {
                    }
            );

            ResponseEntity<List<WatchItem>> stars =
                    restTemplate.exchange(
                            "https://api.github.com/repos" + tsOntology.getRepositoryName() + "/stargazers",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<WatchItem>>() {
                            }
                    );

            ResponseEntity<List<WatchItem>> forks =
                    restTemplate.exchange(
                            "https://api.github.com/repos" + tsOntology.getRepositoryName() + "/forks",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<WatchItem>>() {
                            }
                    );


            releases =
                    restTemplate.exchange(
                            "https://api.github.com/repos" + tsOntology.getRepositoryName() + "/releases",
                            HttpMethod.GET,
                            entity,
                            new ParameterizedTypeReference<List<Releases>>() {
                            }
                    );

            if(releases.getBody().size()>0){
                haveReleases = true;
                releaseVal = 33.33f;
            }else{
                releaseVal = 0.0f;
            }

            try{
                ResponseEntity<ReadMeItem> readme =
                        restTemplate.exchange(
                                "https://api.github.com/repos" + tsOntology.getRepositoryName() + "/readme",
                                HttpMethod.GET,
                                entity,
                                new ParameterizedTypeReference<ReadMeItem>() {}
                        );

                if (readme.hasBody()) {
                    haveReadMe = true;
                    readMeVal = 33.33f;
                }

            }catch (Exception e){
                haveReadMe = false;
                readMeVal = 0.0f;
            }

            try{
                license =
                        restTemplate.exchange(
                                "https://api.github.com/repos" + tsOntology.getRepositoryName() + "/license",
                                HttpMethod.GET,
                                entity,
                                new ParameterizedTypeReference<LicenseItem>() {}
                        );

                if (license.hasBody()) {
                    haveLicense = true;
                    licenseVal = 33.33f;
                }

            }catch (Exception e){
                haveLicense = false;
                licenseVal = 0.0f;
            }


            estimatedValue = releaseVal + licenseVal + readMeVal;


            gitFinalResponsesArray.add(GitFinalResponse.builder().ontologyId(tsOntology.getOntologyId()).title(tsOntology.getTitle()).repoUrl(tsOntology.getRepoUrl()).forks(forks.getBody().size()).watches(whatches.getBody().size()).likes(stars.getBody().size()).releases(haveReleases).readMe(haveReadMe).license(haveLicense).booleanEstimation(estimatedValue).build());
            body = whatches.getBody();

            if (Objects.isNull(body)) {
                System.out.println("can not parsing");
            }

        }
        return new ArrayList<>(gitFinalResponsesArray);

    }

}

