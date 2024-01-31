package eu.tib.tiva.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class OriginOfValueAddedInGrossImport {

    List<ValueAndTradeFlow> grossExports;
}
