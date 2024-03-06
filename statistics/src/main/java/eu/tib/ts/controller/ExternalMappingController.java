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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Operation(summary = "Mappings between multiple uploaded ontology files")
    @Async
    @PostMapping(value="/eccenca", produces= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PagedModel<ExternalMappingModel>> getMappingsLoadOntologyFiles(
            @Parameter(description = "Source ontology file path", example = "one file path")
            @RequestParam("sourceFile") MultipartFile sourceFile,
            @Parameter(description = "List of target ontology file paths", example = "one or more file paths")
            @RequestParam("targetFiles") MultipartFile[] targetFiles,
            @Parameter(description = "Enable or disable to check classes satisfiability using HermiT reasoner", example = "true, false")
            @RequestParam boolean sat,
            Pageable pageable
    ) {

        ProcessedOntology sourceProcessedOntology = preProcessingOntologyService.preProcessMultipartFile(Optional.empty(), sourceFile,
                "external source ontology as a file");

        log.info("satisfiability : " + sat);

        log.info(" source file name: " + sourceFile.getOriginalFilename() + "source file content type: " + sourceFile.getContentType());

        int i=1;

        log.info("number of target files: " + targetFiles.length);

        for(MultipartFile file: targetFiles) {

        log.info(i++ + ".",  " target file name: " + file.getOriginalFilename() + "target file content type: " + file.getContentType());

        }



        Page<ExternalMapping> eternalMappingPage = null;

        PagedModel<ExternalMappingModel> pagedModel = PageUtils.toPagedModel(
                eternalMappingPage,
                ExternalMappingModel.class,
                externalMappingPagedResourcesAssembler,
                externalMappingModelAssembler
        );

    return ResponseEntity.ok(pagedModel);

    }

    @PostMapping(value="/test/upload")
    public ResponseEntity<Map<String, String>> produceMultipartFileMapping(
            @Parameter(description = "List of multipart file paths", example = "file path")
            @RequestPart(value = "files") MultipartFile[] files,
            @Parameter(description = "Enable or disable to check classes satisfiability using HermiT reasoner", example = "true, false")
            @RequestParam boolean sat,
            Pageable pageable
            ){

   Map<String, String> filesMap = new HashMap<>();

   int i=1;

   log.info("files length: " + files.length);

   for(MultipartFile file: files) {

   log.info(i++ + ".",  " file original name: " + file.getOriginalFilename() + " file content type: " + file.getContentType() + " sat: " + sat);

   filesMap.put(i++ + ".",  " file original name: " + file.getOriginalFilename() + " file content type: " + file.getContentType() + " sat: " + sat);

  }

  return ResponseEntity.ok(filesMap);

  }

}