package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class OriginOfValueAddedInFinalDemand {

    String id;
    String countryCode;
    String industryCode;
    String value;
    String year;

}