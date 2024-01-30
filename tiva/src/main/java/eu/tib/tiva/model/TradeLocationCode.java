package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class TradeLocationCode {

    String id;
    List<String> tradeLocationList;
}
