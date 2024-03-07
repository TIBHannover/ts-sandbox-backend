package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.ExternalMappingModelAssembler;
import eu.tib.ts.controller.dto.ExternalMappingModel;
import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.service.ExternalMappingService;
import eu.tib.ts.service.PreProcessingOntologyService;
import eu.tib.ts.utils.HttpUtils;
import eu.tib.ts.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("api/ontology/mapping")
public class ExternalMappingController {

    private final PreProcessingOntologyService preProcessingOntologyService;
    private final ExternalMappingService externalMappingService;
    private final PagedResourcesAssembler<ExternalMapping> externalMappingPagedResourcesAssembler;

    private final ExternalMappingModelAssembler externalMappingModelAssembler;

    @Autowired
    public ExternalMappingController(
        PreProcessingOntologyService preProcessingOntologyService,
        ExternalMappingService externalMappingService,
        PagedResourcesAssembler<ExternalMapping> externalMappingPagedResourcesAssembler,
        ExternalMappingModelAssembler externalMappingModelAssembler){
        this.preProcessingOntologyService=preProcessingOntologyService;
        this.externalMappingService=externalMappingService;
        this.externalMappingPagedResourcesAssembler=externalMappingPagedResourcesAssembler;
        this.externalMappingModelAssembler = externalMappingModelAssembler;
    }
    @Operation(summary = "Mappings between an external ontology and a set of selected TIB TS ontologies")
    @GetMapping(value = "/external/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ExternalMappingModel>> getMappingsForExternalOntologyUri(
            @Parameter(description = "External resolvable ontology URI")
            @RequestParam String uri,
            @Parameter(description = "Set of selected ontologies from TIB TS", example = "dr,coy,cidoc")
            @RequestParam Optional<List<String>> ids,
            @Parameter(description = "Enable or disable to check classes satisfiability using HermiT reasoner", example = "true, false")
            @RequestParam boolean sat,
            Pageable pageable
    ) throws OWLOntologyCreationException {

        ProcessedOntology externalOntology = preProcessingOntologyService.preProcess(Optional.empty(), uri,
                "get mappings between external ontology and selected ontologies from TIB Terminology Service");

        Page<ExternalMapping> eternalMappingPage = externalMappingService.getMappingsForExternalOntology(externalOntology,ids,sat, pageable);

        PagedModel<ExternalMappingModel> pagedModel = PageUtils.toPagedModel(
                eternalMappingPage,
                ExternalMappingModel.class,
                externalMappingPagedResourcesAssembler,
                externalMappingModelAssembler
        );

        return HttpUtils.ok(pagedModel);
    }

    @Operation(summary = "Mappings between two external ontologies given by URLs")
    @GetMapping(value = "/external/pairwise", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ExternalMappingModel>> getMappingsBetweenTwoExternalOntologyUrls(
            @Parameter(description = "The first external resolvable ontology URL (raw file)")
            @RequestParam String sourceUrl,
            @Parameter(description = "The second external resolvable ontology URL (raw file)")
            @RequestParam String targetUrl,
            @Parameter(description = "Enable or disable to check classes satisfiability using HermiT reasoner", example = "true, false")
            @RequestParam boolean sat,
            Pageable pageable
    ) throws OWLOntologyCreationException {

        ProcessedOntology sourceProcessedOntology = preProcessingOntologyService.preProcess(Optional.empty(), sourceUrl,
                "processed source ontology");

        ProcessedOntology targetProcessedOntology = preProcessingOntologyService.preProcess(Optional.empty(), targetUrl,
                "processed target ontology");

        Page<ExternalMapping> eternalMappingPage = externalMappingService.getMappingsBetweenTwoExternalOntologies(sourceProcessedOntology,
                targetProcessedOntology, sat, pageable);

        PagedModel<ExternalMappingModel> pagedModel = PageUtils.toPagedModel(
                eternalMappingPage,
                ExternalMappingModel.class,
                externalMappingPagedResourcesAssembler,
                externalMappingModelAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "Mappings between uploaded ontology files from local machine")
    @PostMapping(value="/eccenca")
    public ResponseEntity<Map<String, String>> produceMultipartFileMapping(
            @Parameter(description = "Source ontology file path", example = "one file path")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "List of multipart file paths", example = "file path")
            @RequestPart(value = "files") MultipartFile[] files,
            @Parameter(description = "Enable or disable to check classes satisfiability using HermiT reasoner", example = "true, false")
            @RequestParam boolean sat,
            Pageable pageable
            ) {

    List<ProcessedOntology> targetProcessedOntologyList = new ArrayList<>();



   Map<String, String> filesMap = new HashMap<>();

   int i=1;

   log.info("satisfiability: " + sat);

   log.info("source ontology original file name: " + file.getOriginalFilename() + " source ontology file content type: " + file.getContentType());

   log.info("files length: " + files.length);

   filesMap.put(i++ + ".",  " source ontology original file name: " + file.getOriginalFilename() + " source ontology file content type: " + file.getContentType());

   ProcessedOntology sourceProcessedOntology = preProcessingOntologyService.preProcessMultipartFile(Optional.empty(), file, file.getOriginalFilename());
   log.info("sourceProcessedOntology.getOntologyId(): " + sourceProcessedOntology.getOntologyId() + " sourceProcessedOntology.getUri(): "+ sourceProcessedOntology.getUri());

   for(MultipartFile f: files) {

   ProcessedOntology targetProcessedOntology = preProcessingOntologyService.preProcessMultipartFile(Optional.empty(), f, f.getOriginalFilename());

   targetProcessedOntologyList.add(targetProcessedOntology);

   log.info(i++ + ".",  " target ontology original file name: " + f.getOriginalFilename() + " target ontology file content type: " + f.getContentType());

   filesMap.put(i++ + ".",  " target ontology original file name: " + f.getOriginalFilename() + " target ontology file content type: " + f.getContentType());

  }

//  OntModel model = ModelFactory.createOntologyModel();
//  InputStream in = file.getInputStream();
//  model.read(in,null, "TURTLE");
//  in.close();
//  InputStream inOwl = file.getInputStream();
// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
// OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(inOwl);
// log.info("owlOntology.getOntologyID() :  " + owlOntology.getOntologyID());
// inOwl.close();

  log.info(String.valueOf("size of targetProcessedOntologyList: " + targetProcessedOntologyList.size()));

 log.info("target ontology titles: ");

for(ProcessedOntology po: targetProcessedOntologyList){

    log.info(po.getTitle());
}

return ResponseEntity.ok(filesMap);

}

}