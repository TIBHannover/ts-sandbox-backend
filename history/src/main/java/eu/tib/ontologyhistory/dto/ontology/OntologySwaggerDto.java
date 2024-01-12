package eu.tib.ontologyhistory.dto.ontology;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.tib.ontologyhistory.model.CommitStatus;

import java.time.Instant;

public record OntologySwaggerDto(
        String id,

        String url,

        String name,

        String description,

        CommitStatus commitStatus,

        String type,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant atime
) {}
