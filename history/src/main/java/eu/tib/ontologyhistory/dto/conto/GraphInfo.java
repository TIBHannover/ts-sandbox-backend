package eu.tib.ontologyhistory.dto.conto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record GraphInfo (
        String name,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant datetime,

        Integer status
) {

}
