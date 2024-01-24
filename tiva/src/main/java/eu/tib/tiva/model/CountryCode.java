package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CountryCode {

    int id;
    String countryCodeId;
}
