package eu.tib.ts.assessments.controller;

import eu.tib.ts.assessments.model.tags.QualityAssessmentDetails;
import eu.tib.ts.assessments.repository.QualityAssessment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssessmentController {

    @Autowired
    QualityAssessment qualityAssessment;

    @GetMapping("/welcome")
    public String login() {
        return "Welcome to login screen";
    }

    @GetMapping("/getQualityAssessment")
    public List<QualityAssessmentDetails> getQualityAssessment() {
        return qualityAssessment.findAll();
    }

}
