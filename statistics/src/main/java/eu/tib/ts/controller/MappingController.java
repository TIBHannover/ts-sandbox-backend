package eu.tib.ts.controller;

import eu.tib.ts.controller.dto.MappingResponseDto;
import eu.tib.ts.controller.dto.PaginatedResponse;
import eu.tib.ts.service.ProcessedMappingService;
import eu.tib.ts.utils.HttpUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api")
public class MappingController {

    private final ProcessedMappingService processedMappingService;

    @Autowired
    public MappingController(ProcessedMappingService processedMappingService) {
        this.processedMappingService = processedMappingService;
    }

    /**
     * Get all mappings with pagination
     */
    @Operation(summary="Get all mappings with pagination")
    @GetMapping(value = "/mappings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<MappingResponseDto>> getAllMappingsPaginated(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        log.info("Fetching all mappings - page: {}, pageSize: {}", page, pageSize);
        PaginatedResponse<MappingResponseDto> response = processedMappingService.getAllMappingsPaginated(page, pageSize);
        return HttpUtils.ok(response);
    }

    /**
     * Get mappings filtered by source ontology with pagination
     */
    @Operation(summary="Get mappings by source ontology with pagination")
    @GetMapping(value = "/mappings/source/{ontologyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedResponse<MappingResponseDto>> getMappingsBySourceOntologyPaginated(
            @Parameter(description = "Source ontology ID", example = "chebi")
            @PathVariable String ontologyId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "50")
            @RequestParam(defaultValue = "50") int pageSize
    ) {
        log.info("Fetching mappings for source ontology: {} - page: {}, pageSize: {}", ontologyId, page, pageSize);
        PaginatedResponse<MappingResponseDto> response = processedMappingService.getMappingsBySourceOntologyPaginated(ontologyId, page, pageSize);
        return HttpUtils.ok(response);
    }
}