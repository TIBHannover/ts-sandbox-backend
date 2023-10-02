package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.OntologyDto;

import lombok.Builder;
import lombok.Value;

import org.springframework.data.util.Pair;
import java.util.List;

@Value
@Builder
public class Similarity {
    Pair<String,String> objects;
    List<OntologyDto> ontologies;
}