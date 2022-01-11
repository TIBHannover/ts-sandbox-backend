package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.Config;
import eu.tib.ts.model.ontology.Ontology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;

import java.util.List;
import java.util.Set;

public class TestData {
    protected List<Ontology> getOntologies() {
        Ontology ontology0 = TsOntology.builder()
            .ontologyId("ontology_0")
            .config(Config.builder().fileLocation("https://something0.ttl").build())
            .build();

        Ontology ontology1 = TsOntology.builder()
            .ontologyId("ontology_1")
            .config(Config.builder().fileLocation("https://something1.ttl").build())
            .build();

        Ontology ontology2 = TsOntology.builder()
            .ontologyId("ontology_2")
            .config(Config.builder().fileLocation("https://something2.ttl").build())
            .build();

        return List.of(ontology0, ontology1, ontology2);
    }

    protected List<ProcessedOntology> getProcessedOntologies() {
        ProcessedOntology processedOntology0 = ProcessedOntology.builder()
            .id(0)
            .ontologyId("ontology_0")
            .uri("https://something0.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_1", "propertyUri_20"))
            .classes(Set.of("classUri_0", "classUri_1", "classUri_20"))
            .build();

        ProcessedOntology processedOntology1 = ProcessedOntology.builder()
            .id(1)
            .ontologyId("ontology_1")
            .uri("https://something1.ttl")
            .properties(Set.of("propertyUri_0", "propertyUri_10", "propertyUri_21"))
            .classes(Set.of("classUri_0", "classUri_10", "classUri_21"))
            .build();

        ProcessedOntology processedOntology2 = ProcessedOntology.builder()
            .id(2)
            .ontologyId("ontology_2")
            .uri("https://something2.ttl")
            .properties(Set.of("propertyUri_100", "propertyUri_200", "propertyUri_300"))
            .classes(Set.of("classUri_100", "classUri_200", "classUri_300"))
            .build();

        return List.of(processedOntology0, processedOntology1, processedOntology2);
    }
}
