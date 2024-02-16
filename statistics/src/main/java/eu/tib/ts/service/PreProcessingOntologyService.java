package eu.tib.ts.service;

import eu.tib.ts.model.ontology.ProcessedOntology;
import eu.tib.ts.model.ontology.TsOntology;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface PreProcessingOntologyService {

    ProcessedOntology preProcess(Optional<TsOntology> tsOntology, String fileLocation, String title);

    ProcessedOntology preProcessMultipartFile(Optional<TsOntology> tsOntology, MultipartFile multipartFile, String title );

}
