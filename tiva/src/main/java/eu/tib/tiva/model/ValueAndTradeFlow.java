package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValueAndTradeFlow {

    String locationCode;
    String industryCode;


    public ValueAndTradeFlow(String locationCode, String industryCode){

        this.locationCode = locationCode;

        this.industryCode=industryCode;
    }
}