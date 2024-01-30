package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.List;

@Setter
@Getter
public class OriginOfValueAddedModel {

    String id;
    String value;
    String year;
    List<ValueAndTradeFlow> valueAddeOrigin;
}