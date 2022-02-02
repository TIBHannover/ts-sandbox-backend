package eu.tib.ts.service.impl;

import eu.tib.ts.model.chart.ChartData;
import eu.tib.ts.model.chart.ChartRequest;
import eu.tib.ts.model.ontology.CharacteristicsInfo;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.PairwiseSimilarity;
import eu.tib.ts.service.ChartService;
import eu.tib.ts.service.SimilarityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChartServiceImplTest {
    @Mock
    private SimilarityService similarityService;

    private ChartService chartService;

    @BeforeEach
    void setUp() {
        chartService = new ChartServiceImpl(similarityService);
    }

    @Test
    void testChart() {
        CharacteristicsInfo characteristicsInfo0 = CharacteristicsInfo.of(List.of("prop1", "prop2"), 100, 100);
        CharacteristicsInfo characteristicsInfo1 = CharacteristicsInfo.of(List.of("import1", "import2"), 100, 100);
        CharacteristicsInfo characteristicsInfo2 = CharacteristicsInfo.of(List.of("class1", "class2"), 100, 100);
        CharacteristicsInfo characteristicsInfo3 = CharacteristicsInfo.of(List.of(), 0, 0);
        CharacteristicsInfo characteristicsInfo4 = CharacteristicsInfo.of(List.of(), 0, 0);

        PairwiseSimilarity pairwiseSimilarity = PairwiseSimilarity.builder()
            .pair(Pair.of("ont1", "ont2"))
            .characteristics(
                Map.of(
                    CharacteristicsType.PROPERTY.name().toLowerCase(), characteristicsInfo0,
                    CharacteristicsType.IMPORT.name().toLowerCase(), characteristicsInfo1,
                    CharacteristicsType.CLASS.name().toLowerCase(), characteristicsInfo2,
                    CharacteristicsType.NAMESPACE.name().toLowerCase(), characteristicsInfo3,
                    CharacteristicsType.INDIVIDUAL.name().toLowerCase(), characteristicsInfo4
                )
            )
            .build();
        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<PairwiseSimilarity> page = new PageImpl<>(List.of(pairwiseSimilarity), pageRequest, 1);

        when(similarityService.getPairwiseSimilarity(any(Optional.class), any(Optional.class), any(Pageable.class)))
            .thenReturn(page);

        ChartRequest chartRequest = ChartRequest.builder()
            .width(Optional.of(800))
            .height(Optional.of(600))
            .horizontal(Optional.of(Boolean.TRUE))
            .build();

        ChartData chart = chartService.chart(Optional.empty(), Optional.empty(), chartRequest, pageRequest);

        assertNotNull(chart);
    }
}
