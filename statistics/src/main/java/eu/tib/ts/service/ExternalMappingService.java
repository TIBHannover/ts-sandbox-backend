package eu.tib.ts.service;

import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ExtendedOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ExternalMappingService {

    <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology,
                                                                             Optional<String> collection,
                                                                             Pageable pageable) throws OWLOntologyCreationException;

}
