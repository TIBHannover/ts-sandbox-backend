package eu.tib.ts.model.ontology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import org.springframework.util.CollectionUtils;
import uk.ac.ox.krr.logmap2.Parameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TsMapping {
    String mappingId;
    String loaded;
    String updated;
    String status;
    String message;
    Object version;
    String fileHash;

}