package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedMapping;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.Optional;
import java.util.Set;

public interface PreProcessingMappingService {

    ProcessedMapping preProcess(Set<MappingObjectStr> MappingObjectStrSet, OWLOntology sourceOntology, OWLOntology targetOntology);
}
