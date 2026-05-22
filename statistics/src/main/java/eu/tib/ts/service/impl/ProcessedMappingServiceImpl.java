package eu.tib.ts.service.impl;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import eu.tib.ts.controller.dto.*;
import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.repository.ProcessedMongoMappingRepository;
import eu.tib.ts.service.ProcessedMappingService;
import eu.tib.ts.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessedMappingServiceImpl implements ProcessedMappingService {

    private final ProcessedMongoMappingRepository repository;

    @Autowired
    public ProcessedMappingServiceImpl(ProcessedMongoMappingRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public SequenceGeneratorService sequenceGeneratorService;

    @Override
    public ProcessedMapping save(ProcessedMapping processedMapping) {
        return repository.save(processedMapping);
    }

    @Override
    public PaginatedResponse<MappingResponseDto> getAllMappingsPaginated(int page, int pageSize) {
        List<ProcessedMapping> allMappings = Lists.newArrayList(repository.findAll());
        return convertToPaginatedResponse(allMappings, page, pageSize);
    }

    @Override
    public PaginatedResponse<MappingResponseDto> getMappingsBySourceOntologyPaginated(String ontologyId, int page, int pageSize) {
        List<ProcessedMapping> allMappings = Lists.newArrayList(repository.findAll());
        
        // Filter mappings by source ontology ID
        List<ProcessedMapping> filteredMappings = allMappings.stream()
                .filter(mapping -> mapping.getSourceOntologyObjectSetModelSet() != null &&
                        mapping.getSourceOntologyObjectSetModelSet().stream()
                                .anyMatch(source -> ontologyId.equals(source.getOntologyId())))
                .collect(Collectors.toList());
        
        return convertToPaginatedResponse(filteredMappings, page, pageSize);
    }

    /**
     * Convert ProcessedMapping list to PaginatedResponse of MappingResponseDto
     * Calculates all missing statistics and enriches data
     */
    private PaginatedResponse<MappingResponseDto> convertToPaginatedResponse(List<ProcessedMapping> mappings, int page, int pageSize) {
        // Apply pagination
        Pageable pageable = PageRequest.of(page, pageSize);
        PageImpl<ProcessedMapping> pageImpl = PageUtils.toPage(mappings, pageable);
        
        // Convert to MappingResponseDto and calculate statistics
        List<MappingResponseDto> responseList = pageImpl.getContent().stream()
                .map(this::convertToMappingResponseDto)
                .collect(Collectors.toList());
        
        return PaginatedResponse.<MappingResponseDto>builder()
                .data(responseList)
                .page(page)
                .pageSize(pageSize)
                .totalCount(mappings.size())
                .build();
    }

    /**
     * Convert ProcessedMapping to MappingResponseDto with calculated statistics and enriched data
     */
    private MappingResponseDto convertToMappingResponseDto(ProcessedMapping mapping) {
        // Get source ontology (typically the first one, or primary)
        OntologyDto sourceOntology = null;
        if (mapping.getSourceOntologyObjectSetModelSet() != null && !mapping.getSourceOntologyObjectSetModelSet().isEmpty()) {
            SourceOntologyObjectSetModel sourceModel = mapping.getSourceOntologyObjectSetModelSet().iterator().next();
            sourceOntology = OntologyDto.builder()
                    .id(sourceModel.getId())
                    .ontologyId(sourceModel.getOntologyId())
                    .uri(sourceModel.getUri())
                    .title(sourceModel.getTitle())
                    .collection(sourceModel.getCollection() != null ? sourceModel.getCollection() : new HashSet<>())
                    .description(sourceModel.getDescription())
                    .build();
        }
        
        // Calculate statistics
        int numberOfTargetOntologies = mapping.getTargetOntologyList() != null ? mapping.getTargetOntologyList().size() : 0;
        int numberOfMappings = calculateNumberOfMappings(mapping);
        int numberOfConflictiveMappings = calculateNumberOfConflictiveMappings(mapping);
        
        StatisticsDto statistics = StatisticsDto.builder()
                .numberOfTargetOntologies(numberOfTargetOntologies)
                .numberOfMappings(numberOfMappings)
                .numberOfConflictiveMappings(numberOfConflictiveMappings)
                .build();
        
        // Convert target ontologies with enriched labels
        Set<TargetOntologyObjectSetModel> targetOntologyList = convertTargetOntologies(mapping.getTargetOntologyList());
        
        return MappingResponseDto.builder()
                .id(String.valueOf(mapping.getId()))
                .mappingId(mapping.getMappingId())
                .sourceOntology(sourceOntology)
                .statistics(statistics)
                .targetOntologyList(targetOntologyList)
                .metadata(MappingMetadataDto.builder()
                        .matchingTool("TIB Terminology Service")
                        .computedAt(mapping.getCreatedAt())
                        .version("1.0")
                        .build())
                .build();
    }

    /**
     * Convert target ontology models and populate statistics, labels, and collection data
     */
    private Set<TargetOntologyObjectSetModel> convertTargetOntologies(Set<TargetOntologyObjectSetModel> targetOntologies) {
        if (targetOntologies == null) {
            return new HashSet<>();
        }
        
        return targetOntologies.stream()
                .peek(target -> {
                    // Ensure targetOntology is properly set with collection and description
                    OntologyDto currentOntology = target.getTargetOntology();
                    if (currentOntology == null && target.getId() > 0) {
                        // Create default ontology if missing
                        target.setTargetOntology(OntologyDto.builder()
                                .id(target.getId())
                                .collection(new HashSet<>())
                                .build());
                    } else if (currentOntology != null) {
                        // Ensure collection is not null by rebuilding if necessary
                        if (currentOntology.getCollection() == null) {
                            OntologyDto enrichedOntology = OntologyDto.builder()
                                    .id(currentOntology.getId())
                                    .ontologyId(currentOntology.getOntologyId())
                                    .uri(currentOntology.getUri())
                                    .title(currentOntology.getTitle())
                                    .collection(new HashSet<>())
                                    .description(currentOntology.getDescription())
                                    .build();
                            target.setTargetOntology(enrichedOntology);
                        }
                    }
                    
                    // Calculate statistics if not already set
                    if (target.getStatistics() == null) {
                        int numMappings = target.getMappingList() != null ? target.getMappingList().size() : 0;
                        int numConflictiveMappings = target.getConflictiveMappingList() != null ? target.getConflictiveMappingList().size() : 0;
                        
                        target.setStatistics(StatisticsDto.builder()
                                .numberOfMappings(numMappings)
                                .numberOfConflictiveMappings(numConflictiveMappings)
                                .numberOfTargetOntologies(1)
                                .build());
                    }
                    
                    // Enrich mappings with labels from IRIs
                    if (target.getMappingList() != null) {
                        target.getMappingList().forEach(mapping -> {
                            if (mapping.getSourceLabel() == null || mapping.getSourceLabel().isEmpty()) {
                                mapping.setSourceLabel(extractLabelFromIRI(mapping.getSourceIRI()));
                            }
                            if (mapping.getTargetLabel() == null || mapping.getTargetLabel().isEmpty()) {
                                mapping.setTargetLabel(extractLabelFromIRI(mapping.getTargetIRI()));
                            }
                        });
                    }
                    
                    // Enrich conflictive mappings with labels from IRIs
                    if (target.getConflictiveMappingList() != null) {
                        target.getConflictiveMappingList().forEach(mapping -> {
                            if (mapping.getSourceLabel() == null || mapping.getSourceLabel().isEmpty()) {
                                mapping.setSourceLabel(extractLabelFromIRI(mapping.getSourceIRI()));
                            }
                            if (mapping.getTargetLabel() == null || mapping.getTargetLabel().isEmpty()) {
                                mapping.setTargetLabel(extractLabelFromIRI(mapping.getTargetIRI()));
                            }
                        });
                    }
                })
                .collect(Collectors.toSet());
    }

    /**
     * Extract label from IRI by getting the fragment or last part after # or /
     */
    private String extractLabelFromIRI(String iri) {
        if (iri == null || iri.isEmpty()) {
            return null;
        }
        
        String label = iri;
        
        // Try to get fragment (part after #)
        if (iri.contains("#")) {
            label = iri.substring(iri.lastIndexOf("#") + 1);
        } else if (iri.contains("/")) {
            label = iri.substring(iri.lastIndexOf("/") + 1);
        }
        
        // Clean up any special characters or encoding
        label = label.replaceAll("%20", " ").replaceAll("_", " ");
        
        return label.isEmpty() ? null : label;
    }

    /**
     * Calculate total number of mappings across all target ontologies
     */
    private int calculateNumberOfMappings(ProcessedMapping mapping) {
        if (mapping.getTargetOntologyList() == null) {
            return 0;
        }
        return mapping.getTargetOntologyList().stream()
                .mapToInt(target -> target.getMappingList() != null ? target.getMappingList().size() : 0)
                .sum();
    }

    /**
     * Calculate total number of conflictive mappings across all target ontologies
     */
    private int calculateNumberOfConflictiveMappings(ProcessedMapping mapping) {
        if (mapping.getTargetOntologyList() == null) {
            return 0;
        }
        return mapping.getTargetOntologyList().stream()
                .mapToInt(target -> target.getConflictiveMappingList() != null ? target.getConflictiveMappingList().size() : 0)
                .sum();
    }
}