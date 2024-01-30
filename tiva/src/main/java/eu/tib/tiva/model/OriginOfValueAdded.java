package eu.tib.tiva.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OriginOfValueAdded {

    String id;
    String value;
    String year;
    List<ValueAndTradeFlow> valueAddeOrigin;
}