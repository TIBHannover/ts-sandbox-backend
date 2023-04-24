package eu.tib.ts.model.ontology;

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

import java.util.Collection;


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

@Field(name = "ontology_id")
private String ontologyId;

@Field(name="mapping_id")
private String mappingId;

@ElementCollection
@CollectionTable(name="sourceOntology", joinColumns = @JoinColumn(name ="id"))
@Field("sourceOntology")
private OWLOntology sourceOntology;

@ElementCollection
@CollectionTable(name="targetOntology", joinColumns = @JoinColumn(name ="id"))
@Field("targetOntology")
private OWLOntology targetOntology;

@ElementCollection
@CollectionTable(name="mappingObjectStrs", joinColumns = @JoinColumn(name="id"))
@Field("mappingObjectStrs")
private Collection<MappingObjectStr> mappingObjectStrs;

    @Field
    private String createdAt = "ZonedDateTimeToDateConverter.INSTANCE";

    @Field
    private String updatedAt = "DateToZonedDateTimeConverter.INSTANCE";

}