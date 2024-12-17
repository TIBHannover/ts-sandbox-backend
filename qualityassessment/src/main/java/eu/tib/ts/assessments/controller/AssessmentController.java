package eu.tib.ts.assessments.controller;

import eu.tib.ts.assessments.model.tags.DataAssessmentDetails;
import eu.tib.ts.assessments.model.tags.CommunityAssessmentDetails;
import eu.tib.ts.assessments.repository.DataAssessment;
import eu.tib.ts.assessments.repository.CommunityAssessment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssessmentController {


    @Autowired
    DataAssessment dataAssessment;

    @Autowired
    CommunityAssessment communityAssessment;

    @GetMapping("/welcome")
    public String login() {
        return "Welcome to login screen";
    }


    @GetMapping("/getDataAssessment")
    public List<DataAssessmentDetails> getDataAssessment() {
        return dataAssessment.findAll();
    }


    @GetMapping("/getCommunityAssessment")
    public List<CommunityAssessmentDetails> getCommunityAssessment() {
        return communityAssessment.findAll();
    }

}