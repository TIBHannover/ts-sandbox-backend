package eu.tib.ts.model.ontology;

import java.util.Set;
import java.util.stream.Collectors;

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
    },
    INDIVIDUAL {
        @Override
        public <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology) {
            return ontology.getIndividuals();
        }
    };

    public abstract <T extends ExtendedOntology> Set<String> getCharacteristics(T ontology);

    public <T extends ExtendedOntology> Set<String> getCharacteristicsToLowerCase(T ontology) {
        return getCharacteristics(ontology).stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }
}
