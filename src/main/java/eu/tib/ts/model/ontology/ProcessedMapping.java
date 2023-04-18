package eu.tib.ts.model.ontology;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
public class ProcessedMapping extends ProcessedOntology{

@Transient
public static final String SEQUENCE_NAME = "user_sequence";

@Id
private int id;

@Field(name="mapping_id")
private String mappingId;

    /**
     * logmap-matcher mapping direction: eq (-2), sub(0), sup(-1),
     */
@ElementCollection
@CollectionTable(name ="mappingDirection", joinColumns = @JoinColumn(name = "id"))
@Field("mappingDirection")
private Integer mappingDirection;

    /**
     * logmap-matcher mapping size.
     */
@ElementCollection
@CollectionTable(name="size", joinColumns = @JoinColumn(name="id"))
@Field("size")
private Integer size;

    /**
     * logmap-matcher type of mapping such as mapping between classes (0), dataproperties(1), objectproperties(2), instances(3), unknown(4) etc.
     */
@ElementCollection
@CollectionTable(name="typeOfMapping", joinColumns = @JoinColumn(name="id"))
@Field("typeOfMapping")
private Integer typeOfMapping;

}