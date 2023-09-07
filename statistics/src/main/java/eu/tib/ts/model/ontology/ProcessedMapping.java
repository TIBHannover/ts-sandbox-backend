package eu.tib.ts.model.ontology;

import eu.tib.ts.controller.dto.MappingObjectSetModel;
import eu.tib.ts.controller.dto.OntologyDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;

import java.util.Set;

/**
 * Information about mappings between a pair of ontologies located in MongoDB mongo_processed_mapping collection
 *
 */
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
private Set<OntologyDto> sourceOntology;

@ElementCollection
@CollectionTable(name="targetOntology", joinColumns =   @JoinColumn(name="id"))
@Field("targetOntology")
private Set<OntologyDto> targetOntology;

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