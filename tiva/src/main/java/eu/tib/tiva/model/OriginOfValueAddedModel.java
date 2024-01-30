package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class OriginOfValueAddedModel {

    String id;
    String value;
    String year;
    List<ValueAndTradeFlow> valueAddeOrigin;
    List<ValueAndTradeFlow> finalDemand;

}