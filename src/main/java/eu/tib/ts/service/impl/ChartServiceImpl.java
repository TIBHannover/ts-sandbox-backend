package eu.tib.ts.service.impl;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.Chart;
import eu.tib.ts.model.chart.ChartData;
import eu.tib.ts.model.chart.ChartRequest;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.service.ChartService;
import eu.tib.ts.service.SimilarityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChartServiceImpl implements ChartService {
    private static final MediaType MIME_TYPE = MediaType.IMAGE_PNG;
    public static final int HEIGHT = 900;
    public static final int WIDTH = 800;

    private final SimilarityService similarityService;

    @Autowired
    public ChartServiceImpl(SimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    @Override
    public ChartData chart(Optional<List<String>> ids, Optional<String> collection, ChartRequest request, Pageable pageable) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Page<PairwiseSimilarity> page = ids.isPresent() && ids.get().size() == 1
            ? similarityService.getPairwiseSimilarity(ids.get().get(0), collection, pageable)
            : similarityService.getPairwiseSimilarity(ids, collection, pageable);

        List<PairwiseSimilarity> similarities = page.getContent();

        int height = request.getHeight().orElse(HEIGHT);
        int width = request.getWidth().orElse(WIDTH);

        List<String> labels = similarities.stream()
            .map(PairwiseSimilarity::getPair)
            .map(pair -> String.format("%s-%s", pair.getFirst(), pair.getSecond()))
            .collect(Collectors.toList());

        DataFrame<String, String> data = DataFrame.of(labels, String.class, columns ->
            columns.add("", similarities.stream().map(PairwiseSimilarity::getPercent).collect(Collectors.toList()))
        );

        Chart.create().withBarPlot(data, false, chart -> {
            chart.plot().axes().domain().label()
                .withFont(new Font("Arial", Font.BOLD, 13))
                .withText("Ontologies pairs");
            chart.plot().axes().range(0).withRange(Bounds.of(0, 100)).label().withText("Similarity percentage");
            if (request.getHorizontal().isPresent() && Boolean.TRUE.equals(request.getHorizontal().get())) {
                chart.plot().orient().horizontal();
            }
            chart.title().withText("Pairwise similarity");
            chart.subtitle().withText("TS ontologies");
            chart.writerPng(os, width, height, false);
        });

        return ChartData.builder()
            .data(os.toByteArray())
            .contentType(MIME_TYPE)
            .build();
    }

    @Override
    public ChartData chart1(Optional<List<String>> ids, Optional<String> collection, ChartRequest request, Pageable pageable) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        Page<PairwiseSimilarity> page = ids.isPresent() && ids.get().size() == 1
            ? similarityService.getPairwiseSimilarity(ids.get().get(0), collection, pageable)
            : similarityService.getPairwiseSimilarity(ids, collection, pageable);

        List<PairwiseSimilarity> similarities = page.getContent();

        int height = request.getHeight().orElse(HEIGHT);
        int width = request.getWidth().orElse(WIDTH);

        List<String> names = similarities.stream()
            .map(similarity ->
                String.format("%s-%s, %.2f%%",
                    similarity.getPair().getFirst(), similarity.getPair().getSecond(), similarity.getPercent()
                )
            )
            .collect(Collectors.toList());

        DataFrame<String, String> frame = DataFrame.of(
            names,
            String.class,
            columns ->
                Stream.of(CharacteristicsType.values())
                    .map(CharacteristicsType::name)
                    .map(String::toLowerCase)
                    .forEach(type ->
                        columns.add(
                            type,
                            similarities.stream()
                                .map(PairwiseSimilarity::getCharacteristics)
                                .map(map -> map.get(type).getSize())
                                .collect(Collectors.toList())
                        )
                    )
        );

        Chart.create().withBarPlot(frame, true, chart -> {
            chart.plot().axes().domain().label()
                .withFont(new Font("Arial", Font.BOLD, 13))
                .withText("Ontologies pairs");
            chart.plot().axes().range(0).label().withText("Similarity");
            if (request.getHorizontal().isPresent() && Boolean.TRUE.equals(request.getHorizontal().get())) {
                chart.plot().orient().horizontal();
            }
            chart.title().withText("Pairwise similarity");
            chart.subtitle().withText("TS ontologies");
            chart.legend().on();
            chart.writerPng(os, width, height, false);
        });

        return ChartData.builder()
            .data(os.toByteArray())
            .contentType(MIME_TYPE)
            .build();
    }
}
