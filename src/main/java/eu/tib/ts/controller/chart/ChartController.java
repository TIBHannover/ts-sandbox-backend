package eu.tib.ts.controller.chart;

import eu.tib.ts.model.chart.ChartData;
import eu.tib.ts.model.chart.ChartRequest;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.service.ChartService;
import eu.tib.ts.service.PreProcessingOntologyService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    private final PreProcessingOntologyService preProcessingOntologyService;

    @Autowired
    public ChartController(ChartService chartService,
                           PreProcessingOntologyService preProcessingOntologyService) {
        this.chartService = chartService;
        this.preProcessingOntologyService = preProcessingOntologyService;
    }

    @ApiOperation(value = "Pairwise similarity Bar Chart between TS internal ontologies")
    @GetMapping(value = "/internal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> chart(
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

    @ApiOperation("Pairwise similarity Bar Chart for external ontology")
    @GetMapping(value = "/external", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> chartForExternalOntology(
        Pageable pageable,
        @ApiParam(value = "External ontology URL")
        @RequestParam String url,
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

        ProcessedOntology ontology = preProcessingOntologyService.preProcess(Optional.empty(), url);
        ChartData chartData = chartService.chart(ontology, collection, request, pageable);

        return ResponseEntity.ok()
            .contentLength(chartData.getLength())
            .contentType(chartData.getContentType())
            .body(chartData.getData());
    }
}
