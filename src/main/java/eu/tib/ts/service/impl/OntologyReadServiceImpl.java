package eu.tib.ts.service.impl;

import eu.tib.ts.model.ontology.OntologyType;
import eu.tib.ts.service.OntologyReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OntologyReadServiceImpl implements OntologyReadService {
    private static final int FILE_EXTENSION_LENGTH = 3;

    @Override
    public OntModel readOntology(String uri) {
        OntModel model = ModelFactory.createOntologyModel();
        OntologyType type = OntologyType.get(StringUtils.right(uri, FILE_EXTENSION_LENGTH));
        model.read(uri, type.getName());

        return model;
    }
}
