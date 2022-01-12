package eu.tib.ts.model.ontology;

import java.util.Set;

public enum CharacteristicsType {
    PROPERTY {
        @Override
        public Set<String> getCharacteristics(ProcessedOntology processedOntology) {
            return processedOntology.getProperties();
        }
    },
    CLASS {
        @Override
        public Set<String> getCharacteristics(ProcessedOntology processedOntology) {
            return processedOntology.getClasses();
        }
    },
    NAMESPACE {
        @Override
        public Set<String> getCharacteristics(ProcessedOntology processedOntology) {
            return processedOntology.getNamespaces();
        }
    },
    IMPORT {
        @Override
        public Set<String> getCharacteristics(ProcessedOntology processedOntology) {
            return processedOntology.getImports();
        }
    };

    public abstract Set<String> getCharacteristics(ProcessedOntology processedOntology);
}
