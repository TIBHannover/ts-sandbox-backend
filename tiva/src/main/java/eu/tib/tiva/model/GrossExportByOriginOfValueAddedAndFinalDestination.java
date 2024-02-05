package eu.tib.tiva.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GrossExportByOriginOfValueAddedAndFinalDestination {

    String finalDemandCountryCode;
    List<ValueAndTradeFlow> grossExports;
    String value;
    String year;

}
