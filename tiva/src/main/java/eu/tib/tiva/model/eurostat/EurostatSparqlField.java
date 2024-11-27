package eu.tib.tiva.model.eurostat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EurostatSparqlField {
    REPORTER("reporter"),
    PARTNER("partner"),
    PRODUCT("product"),
    FLOW("flow"),
    VALUE("value"),
    VALUES_SUM("values_sum"),
    YEAR("year"),
    YEAR_TRIMMED("year_trimmed"),

    GEO("geo"),
    GEO_TRIMMED("geo_trimmed"),
    UNIT("unit");

    private final String value;
}
