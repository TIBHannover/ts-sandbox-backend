package eu.tib.ts.model.ontology;

import java.util.Set;

public enum CharacteristicsType {
    PROPERTY {
        @Override
        public <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology) {
            return ontology.getProperties();
        }
    },
    CLASS {
        @Override
        public <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology) {
            return ontology.getClasses();
        }
    },
    NAMESPACE {
        @Override
        public <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology) {
            return ontology.getNamespaces();
        }
    },
    IMPORT {
        @Override
        public <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology) {
            return ontology.getImports();
        }
    };

    public abstract <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology);
}
