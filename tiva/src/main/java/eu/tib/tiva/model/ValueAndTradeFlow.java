package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ValueAndTradeFlow {

    String tradeLocationCode;
    String industryCode;


    public ValueAndTradeFlow(String tradeLocationCode, String industryCode){

        this.tradeLocationCode = tradeLocationCode;

        this.industryCode=industryCode;
    }
}