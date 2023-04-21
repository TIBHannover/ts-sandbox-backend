package eu.tib.ts.model.ontology;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;

import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mongo_processed_mapping")
public class ProcessedMapping{

@Transient
public static final String SEQUENCE_NAME = "user_sequence";

@Id
private int id;


@Field(name="mapping_id")
private String mappingId;

@ElementCollection
@CollectionTable(name="sourceOntologyIRI", joinColumns = @JoinColumn(name ="id"))
@Field("sourceOntologyIRI")
private Set<String> sourceOntologyIRI;

@ElementCollection
@CollectionTable(name="targetOntologyIRI", joinColumns = @JoinColumn(name ="id"))
@Field("targetOntologyIRI")
private Set<String> targetOntologyIRI;

@ElementCollection
@CollectionTable(name="mappingObjectStrs", joinColumns = @JoinColumn(name="id"))
@Field("mappingObjectStrs")
private Set<MappingObjectStr> mappingObjectStrs;

}