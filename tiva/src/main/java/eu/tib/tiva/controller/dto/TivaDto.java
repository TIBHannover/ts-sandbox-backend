package eu.tib.tiva.controller.dto;

import eu.tib.tiva.model.ProcessedTiva;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Builder
@Value
public class TivaDto {

    long id;

    String tivaId;

    String countryCode;

    Set<String> collection;

    public static TivaDto getCountryCodes(ProcessedTiva processedTiva){

        return TivaDto.builder()
                .id(processedTiva.getId())
                .tivaId(processedTiva.getTivaId())
                .countryCode(processedTiva.getCountryCode())
                .build();

    }
    public static TivaDto of(ProcessedTiva processedTiva) {

        return TivaDto.builder()
                .id(processedTiva.getId())
                .tivaId(processedTiva.getTivaId())
                .countryCode(processedTiva.getCountryCode())
                .build();
    }

}
