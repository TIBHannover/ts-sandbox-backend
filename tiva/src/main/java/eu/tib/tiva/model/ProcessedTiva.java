package eu.tib.tiva.model;

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
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mongo_processed_tiva")
public class ProcessedTiva {

    @Transient
    public static final String SQUENCE_NAME="user_sequence";

    @Id
    private int id;

    @Field(name="tiva_id")
    private String tivaId;

    @ElementCollection
    @CollectionTable(name="countryCode",joinColumns= @JoinColumn(name="id"))
    @Field("countryCode")
    private Set<String> countryCode;

}
