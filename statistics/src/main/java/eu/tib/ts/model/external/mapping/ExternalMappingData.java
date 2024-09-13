package eu.tib.ts.model.external.mapping;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Builder
public class ExternalMappingData {

    @Id
    private String id;

    private String uri;

}
