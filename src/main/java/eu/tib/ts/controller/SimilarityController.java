package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.DataResultObject;
import eu.tib.ts.controller.dto.SharedClassUriDto;
import eu.tib.ts.controller.dto.SharedPropertyUriDto;
import eu.tib.ts.model.ontology.ExternalOntology;
import eu.tib.ts.model.ontology.SimpleOntology;
import eu.tib.ts.service.SimilarityService;
import eu.tib.ts.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ontology/similarity")
public class SimilarityController {
    private final SimilarityService similarityService;

    @Autowired
    public SimilarityController(SimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    @GetMapping(value = "/property/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataResultObject<SharedPropertyUriDto>> similarityByPropertyInternal(
        @RequestBody List<SimpleOntology> ontologies
    ) {
        SharedPropertyUriDto dto = similarityService.getSharedPropertyUri(ontologies);

        return HttpUtils.ok(dto);
    }

    @GetMapping(value = "/property/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataResultObject<SharedPropertyUriDto>> similarityByPropertyExternal(
        @RequestBody List<ExternalOntology> ontologies
    ) {
        SharedPropertyUriDto dto = similarityService.getSharedPropertyUri(ontologies);

        return HttpUtils.ok(dto);
    }

    @GetMapping(value = "/class", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataResultObject<SharedClassUriDto>> similarityByClass(
        @RequestBody List<SimpleOntology> ontologies
    ) {
        SharedClassUriDto dto = similarityService.getSharedClassUri(ontologies);

        return HttpUtils.ok(dto);
    }
}
