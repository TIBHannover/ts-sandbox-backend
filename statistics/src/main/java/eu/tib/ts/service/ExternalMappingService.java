package eu.tib.ts.service;

import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ExtendedOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ExternalMappingService {

    /**
     * Mappings between an external ontology given as a resolvable URLs (raw file) and a set of selected ontologies
     * ingested in TIB terminology service (TS).
     * @param ontology
     * @param ids
     * @param sat
     * @param pageable
     * @return
     * @param <T>
     * @throws OWLOntologyCreationException
     */
    <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology,
                                                                             Optional<List<String>> ids, boolean sat,
                                                                             Pageable pageable) throws OWLOntologyCreationException;

    /**
     * Pairwise mappings between two external ontologies given by their resolvable URLs as a raw files.
     * @param sourceOntology
     * @param targetOntology
     * @param sat
     * @param pageable
     * @return
     * @param <T>
     * @throws OWLOntologyCreationException
     */
    <T extends ExtendedOntology> Page<ExternalMapping> getMappingsBetweenTwoExternalOntologies(T sourceOntology,
                                                                                               T targetOntology, boolean sat,
                                                                                      Pageable pageable) throws OWLOntologyCreationException;

}