package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyTraverseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OntologyTraverseServiceImpl implements OntologyTraverseService {

    @Override
    public Set<String> getImports(OWLOntology owlOntology) {
        if (owlOntology == null) {
            return Set.of();
        }

        return owlOntology.importsDeclarations()
            .map(OWLImportsDeclaration::getIRI)
            .map(IRI::toString)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getNamespaces(OWLOntology owlOntology) {
        if (owlOntology == null) {
            return Set.of();
        }

        OWLDocumentFormat format = owlOntology.getOWLOntologyManager().getOntologyFormat(owlOntology);
        Set<String> set = new HashSet<>();
        if (format != null && format.isPrefixOWLDocumentFormat()) {
            set = new HashSet<>(format.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap().values());
        }

        return set;
    }

    @Override
    public Set<String> getProperties(OntModel model) {
        if (model == null) {
            return Set.of();
        }

        return model.listOntProperties().toList().stream()
            .filter(OntProperty::isObjectProperty)
            .map(OntProperty::getURI)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getIndividuals(OWLOntology owlOntology) {
        if (owlOntology == null) {
            return Set.of();
        }

        Set<OWLNamedIndividual> set = new HashSet<>();
        owlOntology.individualsInSignature().forEach(set::add);

        return set.stream()
            .map(OWLNamedIndividual::getIRI)
            .map(IRI::toString)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getClasses(OWLOntology owlOntology) {
        Set<OWLClass> set = new HashSet<>();
        owlOntology.classesInSignature().forEach(set::add);

        return set.stream()
            .map(OWLClass::toString)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getClasses(OntModel model) {
        if (model == null) {
            return Set.of();
        }

        Set<String> classes = new HashSet<>();
        // create an iterator over the root classes
        Iterator<OntClass> iterator = model.listHierarchyRootClasses();

        // traverse through all roots
        while (iterator.hasNext()) {
            OntClass tmp = iterator.next();
            traverse(tmp, new ArrayList<>(), classes);
        }

        return classes;
    }

    private void traverse(OntClass oc, List<OntClass> occurs, Set<String> classes) {
        if (oc == null) {
            return;
        }
        // if end reached abort (Thing == root, Nothing == deadlock)
        if (oc.getLocalName() == null || oc.getLocalName().equals("Nothing")) {
            return;
        }

        classes.add(oc.toString());

        // check if we already visited this OntClass (avoid loops in graphs)
        if (oc.canAs(OntClass.class) && !occurs.contains(oc)) {
            // for every subClass, traverse down
            for (Iterator<OntClass> i = oc.listSubClasses(true); i.hasNext(); ) {
                OntClass subClass = i.next();
                // push this expression on the occurs list before we recurse to avoid loops
                occurs.add(oc);
                // traverse down and increase depth (used for logging tabs)
                traverse(subClass, occurs, classes);
                // after traversing the path, remove from occurs list
                occurs.remove(oc);
            }
        }
    }
}
