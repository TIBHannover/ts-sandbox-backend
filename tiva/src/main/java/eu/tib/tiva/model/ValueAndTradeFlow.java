package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValueAndTradeFlow {

    String countryCode;
    String industryCode;
    String internationalOrganizationCode;

    public ValueAndTradeFlow(String countryCode, String internationalOrganizationCode, String industryCode){

        this.countryCode=countryCode;
        this.internationalOrganizationCode=internationalOrganizationCode;
        this.industryCode=industryCode;
    }
}