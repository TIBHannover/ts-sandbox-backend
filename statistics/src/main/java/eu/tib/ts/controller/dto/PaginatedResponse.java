package eu.tib.ts.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Generic paginated response wrapper for API responses
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private int page;
    private int pageSize;
    private long totalCount;

    public PaginatedResponse() {
    }
}
