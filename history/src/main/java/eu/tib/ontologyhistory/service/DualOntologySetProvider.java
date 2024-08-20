package eu.tib.ontologyhistory.service;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.util.Collections;
import java.util.Set;

public class DualOntologySetProvider implements OWLOntologySetProvider {

    @Serial
    private static final long serialVersionUID = -8942374248162307075L;
    private final Set<OWLOntology> ontologies = Sets.newIdentityHashSet();

    /**
     * Init a new DualOntologySetProvider for a left and right ontology.
     *
     * @param left OWLOntologySetProvider for left ontology
     * @param right OWLOntologySetProvider for right ontology
     */
    public DualOntologySetProvider(OWLOntologySetProvider left, OWLOntologySetProvider right) {
        ontologies.addAll(left.getOntologies());
        ontologies.addAll(right.getOntologies());
    }

    /**
     * Get the ontologies in the provider.
     *
     * @return Set of OWLOntologies
     */
    @Nonnull
    @Override
    public Set<OWLOntology> getOntologies() {
        return Collections.unmodifiableSet(ontologies);
    }
}
