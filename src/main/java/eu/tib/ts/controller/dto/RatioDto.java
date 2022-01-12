package eu.tib.ts.controller.dto;

import lombok.Getter;

@Getter
public class RatioDto {
    private final double value;

    public RatioDto(double value) {
        this.value = value;
    }
}
