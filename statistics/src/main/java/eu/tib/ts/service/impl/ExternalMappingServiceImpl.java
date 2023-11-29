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
    public <T extends ExtendedOntology> Page<ExternalMapping> getMappingsForExternalOntology(T ontology, Optional<String> collection, Pageable pageable) throws OWLOntologyCreationException {

        List<ProcessedOntology> processedOntologies  = getProcessedOntologies();

        if(processedOntologies ==null || processedOntologies.isEmpty()){

        return PageUtils.toPage(Collections.emptyList(), pageable);

        }

        List<ProcessedOntology> filteredOntologies = ontologyFilterService.filter(processedOntologies, collection);

            ProcessedOntology externalOntology = ProcessedOntology.of(ontology);

            String exernalOntologyUri = externalOntology.getUri();

            List<ExternalMapping> externalMappings = new ArrayList<>();

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



            }


//        @Override
//        public <T extends ExtendedOntology> Page< PairwiseSimilarity > getPairwiseSimilarity(T ontology,
//                Optional<String> collection,
//                Pageable pageable) {
//            List<ProcessedOntology> processedOntologies = getProcessedOntologies();
//            if (processedOntologies == null || processedOntologies.isEmpty()) {
//                return PageUtils.toPage(Collections.emptyList(), pageable);
//            }
//            List<ProcessedOntology> filteredOntologies = filterService.filter(processedOntologies, collection);
//            ProcessedOntology ont2 = ProcessedOntology.of(ontology);
//
//            List<PairwiseSimilarity> pairwiseSimilarities = new ArrayList<>();
//            Set<OntologyPair> set = new HashSet<>();
//            for (ProcessedOntology ont1 : filteredOntologies) {
//                OntologyPair pair = OntologyPair.of(ont1, ont2);
//                if (ont1.equalsTsOntology(ont2) || set.contains(pair.inverted())) {
//                    continue;
//                }
//                set.add(pair);
//                PairwiseSimilarity pairwiseSimilarity = processPairs(ont1, ont2);
//                pairwiseSimilarities.add(pairwiseSimilarity);
//            }
//
//            List<PairwiseSimilarity> sorted = pairwiseSimilarities.stream()
//                    .filter(aggregatedSimilarity -> aggregatedSimilarity.getSum() > 0)
//                    .sorted(Comparator.comparing(PairwiseSimilarity::getPercent).reversed())
//                    .collect(Collectors.toList());
//
//            return PageUtils.toPage(sorted, pageable);
//        }

        return PageUtils.toPage(externalMappings, pageable);

    }

    private List<ProcessedOntology> getProcessedOntologies() {

        return StreamSupport.stream(ProcessedMongoOntologyRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(ProcessedOntology::getOntologyId))
                .collect(Collectors.toList());

    }


}
