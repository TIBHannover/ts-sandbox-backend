package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValueAndTradeFlow {

    String countryCode;
    String industryCode;

    public ValueAndTradeFlow(String countryCode, String industryCode){

        this.countryCode=countryCode;
        this.industryCode=industryCode;

    }

}