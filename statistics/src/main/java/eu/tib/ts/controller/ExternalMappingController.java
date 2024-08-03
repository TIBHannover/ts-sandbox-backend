package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.ExternalMappingModelAssembler;
import eu.tib.ts.controller.dto.ExternalMappingModel;
import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import eu.tib.ts.repository.TsRepository;
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
    private final TsRepository tsRepository;

    @Autowired
    public ExternalMappingController(
        TsRepository tsRepository,
        PreProcessingOntologyService preProcessingOntologyService,
        ExternalMappingService externalMappingService,
        PagedResourcesAssembler<ExternalMapping> externalMappingPagedResourcesAssembler,
        ExternalMappingModelAssembler externalMappingModelAssembler){
        this.tsRepository = tsRepository;
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

    @Operation(summary = "Mappings between ontology files uploaded from local machine")
    @PostMapping(value="/eccenca")
    public ResponseEntity<PagedModel<ExternalMappingModel>> produceMultipartFileMapping(
            @Parameter(description = "Source ontology file path", example = "-F file=@C://Users//KrdzavacN//git//ontologies//scor//scor.ttl")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "One or more target ontology file paths", example = "-F files=@C://Users//KrdzavacN//git//ontologies//scor//scor.ttl")
            @RequestPart(value = "files") MultipartFile[] files,
            @Parameter(description = "Enable or disable to check classes satisfiability using HermiT reasoner", example = "true, false")
            @RequestParam boolean sat,
            Pageable pageable
            ) throws OWLOntologyCreationException, IOException {

Page<ExternalMapping> eternalMultipartFileMappingPage = externalMappingService.getMultipartFileMappingMappingForExternalOntology(
                file,
                files,
                sat,
                pageable
                );

        PagedModel<ExternalMappingModel> pagedModel = PageUtils.toPagedModel(
                eternalMultipartFileMappingPage,
                ExternalMappingModel.class,
                externalMappingPagedResourcesAssembler,
                externalMappingModelAssembler
        );

    return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "Computes mappings between given list of ontology ids ingested in TS and other ontologies that " +
            "are ingested in TIB Terminology Service. Mapping results are store into MongoDB.")
    @PostMapping(value="/external/mongodb")
    public ResponseEntity<PagedModel<ExternalMappingModel>> storeComputedMappingsIntoMongoDB(
            @Parameter(description="A list of source ontology ids", example="coy,dr")
            @RequestParam Optional<List<String>> sourceontologyid,
            @RequestParam Optional<List<String>> targetontologyid,
            Pageable pageable
    ) throws  OWLOntologyCreationException, IOException {

    Page<ExternalMapping> mappingsStoredInMongoDB =externalMappingService.getMappingsBetweenOntologyIdsAndAllOtherTSOntologies(sourceontologyid, targetontologyid,pageable);


        PagedModel<ExternalMappingModel> pagedModel = PageUtils.toPagedModel(
                mappingsStoredInMongoDB,
                ExternalMappingModel.class,
                externalMappingPagedResourcesAssembler,
                externalMappingModelAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

}