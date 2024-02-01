package eu.tib.tiva.model;

import java.util.List;

public interface OriginOfValueAddedInGrossImportModel {

    String getId();
    String getImportCountryCode();
    List<ValueAndTradeFlow> getGrossExport();
    String getValue();
    String getYear();

}
