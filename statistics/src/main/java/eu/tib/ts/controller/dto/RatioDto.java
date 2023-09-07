package eu.tib.ts.controller.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class RatioDto {
    double result;
    int similaritiesNumber;
    double distinctCharacteristicsNumber;
}
