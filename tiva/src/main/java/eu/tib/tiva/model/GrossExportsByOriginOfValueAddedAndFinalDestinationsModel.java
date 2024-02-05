package eu.tib.tiva.model;

import java.util.List;

public interface GrossExportsByOriginOfValueAddedAndFinalDestinationsModel {

    String getId();
    String getFinalDemandCountryCode();
    List<ValueAndTradeFlow> getFrossExports();
    String getValue();
    String getYear();

}
