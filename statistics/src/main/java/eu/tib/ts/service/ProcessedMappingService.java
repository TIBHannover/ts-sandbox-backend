package eu.tib.ts.service;

import eu.tib.ts.controller.dto.MappingResponseDto;
import eu.tib.ts.controller.dto.PaginatedResponse;
import eu.tib.ts.model.ontology.ProcessedMapping;

/**
 * Service that calculates mappings between a pair of ontologies, and save mappings to MongoDB
 */
public interface ProcessedMappingService {

    ProcessedMapping save(ProcessedMapping processedMapping);

    /**
     * Get all mappings with pagination
     * @param page page number (0-indexed)
     * @param pageSize number of items per page
     * @return PaginatedResponse containing MappingResponseDto items
     */
    PaginatedResponse<MappingResponseDto> getAllMappingsPaginated(int page, int pageSize);

    /**
     * Get mappings filtered by source ontology with pagination
     * @param ontologyId the source ontology ID
     * @param page page number (0-indexed)
     * @param pageSize number of items per page
     * @return PaginatedResponse containing MappingResponseDto items
     */
    PaginatedResponse<MappingResponseDto> getMappingsBySourceOntologyPaginated(String ontologyId, int page, int pageSize);

}
