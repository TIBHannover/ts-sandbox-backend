package eu.tib.ts.service;

import eu.tib.ts.model.external.mapping.ExternalMapping;
import eu.tib.ts.model.ontology.ExtendedOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    /**
     *
     * Mapping between source ontology and list of target ontologies, given as local file paths. In this implementation
     * we use Multipart upload.
     *
      * @param file file path to source ontology
     * @param files file paths to one or more target ontologies
     * @param sat satisfiability checking yes/no
     * @param pageable pageable
     * @return
     * @param <T>
     * @throws OWLOntologyCreationException
     * @throws IOException
     */
    public <T extends  ExtendedOntology> Page<ExternalMapping> getMultipartFileMappingMappingForExternalOntology(MultipartFile file,
                                                                                                                 MultipartFile[] files,
                                                                                                                 boolean sat,
                                                                                                                 Pageable pageable) throws OWLOntologyCreationException, IOException;


    /**
     * List all ontologies from TIB TS and processed ontologies in mappings i.e. used in mapping
     * computation. These ontologies are available in MongoDB
     */

    public <T extends  ExtendedOntology> Page<ExternalMapping> getAllTIBTSOntologiesAndProcessedOntologiesInMappings(
            List<String> ids,
            Pageable pageable) throws OWLOntologyCreationException, IOException;
}