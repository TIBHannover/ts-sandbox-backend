package eu.tib.ts.controller.chart;

import eu.tib.ts.model.chart.ChartData;
import eu.tib.ts.model.chart.ChartRequest;
import eu.tib.ts.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ontology/similarity/pairwise/chart")
public class ChartController {
    private final ChartService chartService;

    @Autowired
    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping(value = "1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> testChart(
        Pageable pageable,
        @RequestParam(required = false) Optional<List<String>> ids,
        @RequestParam(required = false) Optional<String> collection,
        @RequestParam(required = false) Optional<Boolean> horizontal,
        @RequestParam(required = false) Optional<Integer> height,
        @RequestParam(required = false) Optional<Integer> width
    ) {
        ChartRequest request = ChartRequest.builder()
            .height(height)
            .width(width)
            .horizontal(horizontal)
            .build();

        ChartData chartData = chartService.chart1(ids, collection, request, pageable);

        return ResponseEntity.ok()
            .contentLength(chartData.getLength())
            .contentType(chartData.getContentType())
            .body(chartData.getData());
    }

    @GetMapping(value = "2", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> testChart2(
        Pageable pageable,
        @RequestParam(required = false) Optional<List<String>> ids,
        @RequestParam(required = false) Optional<String> collection,
        @RequestParam(required = false) Optional<Boolean> horizontal,
        @RequestParam(required = false) Optional<Integer> height,
        @RequestParam(required = false) Optional<Integer> width
    ) {
        ChartRequest request = ChartRequest.builder()
            .height(height)
            .width(width)
            .horizontal(horizontal)
            .build();

        ChartData chartData = chartService.chart(ids, collection, request, pageable);

        return ResponseEntity.ok()
            .contentLength(chartData.getLength())
            .contentType(chartData.getContentType())
            .body(chartData.getData());
    }

}
