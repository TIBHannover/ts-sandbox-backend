package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import lombok.*;
import org.semanticweb.owlapi.model.OWLOntology;
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
public class ProcessedMapping {

@Transient
public static final String SEQUENCE_NAME = "user_sequence";

@Id
private int id;

@Field(name="mapping_id")
private String mappingId;

@ElementCollection
@CollectionTable(name="sourceOntology", joinColumns =   @JoinColumn(name="id"))
@Field("sourceOntology")
private String sourceOntology;

@ElementCollection
@CollectionTable(name="targetOntology", joinColumns =   @JoinColumn(name="id"))
@Field("targetOntology")
private String targetOntology;


@ElementCollection
@CollectionTable(name="numberOfMappings", joinColumns = @JoinColumn(name="id"))
@Field("numberOfMappings")
private int numberOfMappings;

@ElementCollection
@CollectionTable(name="numberOfConflictiveMappings", joinColumns = @JoinColumn(name="id"))
@Field("numberOfConflictiveMappings")
private int numberOfConflictiveMappings;

@ElementCollection
@CollectionTable(name="mappingList", joinColumns = @JoinColumn(name="id"))
@Field("mappingList")
private Set<MappingObjectSetModel> mappingList;

@ElementCollection
@CollectionTable(name="conflictiveMappingsList", joinColumns = @JoinColumn(name="id"))
@Field("conflictiveMappingsList")
private Set<MappingObjectSetModel> conflictiveMappingsList;

@Field
private String createdAt = "ZonedDateTimeToDateConverter.INSTANCE";

@Field
private String updatedAt = "DateToZonedDateTimeConverter.INSTANCE";

}