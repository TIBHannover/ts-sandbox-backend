package eu.tib.ts.service.impl;

import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ExtendedOntology;
import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.repository.ProcessedMongoOntologyRepository;
import eu.tib.ts.service.ExternalMappingService;
import eu.tib.ts.service.OntologyFilterService;
import eu.tib.ts.utils.PageUtils;
import lombok.extern.slf4j.Slf4j;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ox.krr.logmap2.LogMap2_Matcher;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class ExternalMappingServiceImpl implements ExternalMappingService {

    private final ProcessedMongoOntologyRepository ProcessedMongoOntologyRepository;

    private final OntologyFilterService ontologyFilterService;

    OWLOntologyManager ontoManagerForExternalOntology;
    OWLOntologyManager ontoManagerForTerminologyServiceOntology;

    @Autowired
    protected ExternalMappingServiceImpl(
            ProcessedMongoOntologyRepository processedMongoOntologyRepository,
            OntologyFilterService ontologyFilterService
    ){

        this.ProcessedMongoOntologyRepository=processedMongoOntologyRepository;
        this.ontologyFilterService=ontologyFilterService;

    }
    @Override
    public <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology, Optional<List<String>> ids, Optional<String> collection, Pageable pageable) throws OWLOntologyCreationException {

        List<ProcessedOntology> processedOntologies = ids.isPresent()
                ? getProcessedOntologies(ids.get())
                : getProcessedOntologies();

        if (processedOntologies == null || processedOntologies.isEmpty()) {
            return PageUtils.toPage(Collections.emptyList(), pageable);
        }

        List<ProcessedOntology> filteredOntologies = ontologyFilterService.filter(processedOntologies, collection);

        ProcessedOntology externalOntology = ProcessedOntology.of(ontology);

        String exernalOntologyUri = externalOntology.getUri();

        List<ExternalMapping> externalMappings = new ArrayList<>();

        int numberOfTargetOntologies=filteredOntologies.size();

        for (ProcessedOntology processedOntology : filteredOntologies) {

                log.info("ontology id: " + processedOntology.getOntologyId() + " ontology uri: " + processedOntology.getUri());

                String terminologyServiceOntologyUri = processedOntology.getUri();

                ontoManagerForExternalOntology  = OWLManager.createOWLOntologyManager();
                ontoManagerForTerminologyServiceOntology  = OWLManager.createOWLOntologyManager();

                OWLOntology externalOWLOntology = ontoManagerForExternalOntology.loadOntology(IRI.create(exernalOntologyUri));
                OWLOntology terminologyServiceOWLOntology = ontoManagerForTerminologyServiceOntology.loadOntology(IRI.create(terminologyServiceOntologyUri));

                log.info("externalOWLOntology.getAxiomCount(): " + externalOWLOntology.getAxiomCount() +
                        " terminologyServiceOWLOntology.getAxiomCount(): " + terminologyServiceOWLOntology.getAxiomCount());

                LogMap2_Matcher logmap2 = new LogMap2_Matcher(externalOWLOntology, terminologyServiceOWLOntology);

                Set<MappingObjectStr> logmap2_mappings= logmap2.getLogmap2_Mappings();

                for(MappingObjectStr mappingObjectStr: logmap2_mappings){


            }



         }

        return PageUtils.toPage(externalMappings, pageable);

    }

    private List<ProcessedOntology> getProcessedOntologies(List<String> ids) {

        return ProcessedMongoOntologyRepository.findByOntologyIdIn(ids);

    }

    private List<ProcessedOntology> getProcessedOntologies() {

        return StreamSupport.stream(ProcessedMongoOntologyRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ProcessedOntology::getOntologyId))
                .collect(Collectors.toList());

    }


}
