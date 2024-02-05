package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.List;

@Getter
@Setter
public class GrossExportByOriginOfValueAddedAndFinalDestination {

    String finalDemandCountryCode;
    List<ValueAndTradeFlow> grossExports;
    String value;
    String year;

}
