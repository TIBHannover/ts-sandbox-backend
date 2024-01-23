package eu.tib.tiva.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder(toBuilder=true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mongo_processed_tiva")
public class ProcessedTiva {


}
