package eu.tib.ts.service.impl;

import eu.tib.ts.service.OntologyTraverseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OntologyTraverseServiceImpl implements OntologyTraverseService {

    @Override
    public Set<String> getImports(OntModel model) {

        return model.listImportedOntologyURIs();
    }

    @Override
    public Set<String> getNamespaces(OntModel model) {

        return model.listNameSpaces().toSet();
    }

    @Override
    public Set<String> getProperties(OntModel model) {

        return model.listOntProperties().toList().stream()
            .filter(OntProperty::isObjectProperty)
            .map(OntProperty::getURI)
            .map(String::toLowerCase)
            .map(s -> s.replace("_", ""))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getClasses(OntModel model) {
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

        classes.add(oc.toString().toLowerCase());

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
