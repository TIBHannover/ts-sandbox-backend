package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class GrossExportByOriginOfValueAddedAndFinalDestination {

    String finalDemandCountryCode;
    List<ValueAndTradeFlow> grossExports;
    String value;
    String year;

}
