package eu.tib.ts.controller.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KeyValueResultDto {
    String key;
    long value;
}
