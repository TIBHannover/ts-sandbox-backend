package eu.tib.ts.service;

import eu.tib.ts.model.chart.ChartData;
import eu.tib.ts.model.chart.ChartRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ChartService {
    ChartData chart(Optional<List<String>> ids, Optional<String> collection, ChartRequest request, Pageable pageable);

    ChartData chart1(Optional<List<String>> ids, Optional<String> collection, ChartRequest request, Pageable pageable);
}
