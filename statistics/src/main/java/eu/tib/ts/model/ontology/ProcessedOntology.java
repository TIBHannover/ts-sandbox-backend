package eu.tib.ts.model.ontology;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.CollectionUtils;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mongo_processed_ontologies")
public class ProcessedOntology implements ExtendedOntology {

    @Transient
    public static final String SEQUENCE_NAME = "user_sequence";

    @Id
    private int id;

    @Field(name = "ontology_id")
    private String ontologyId;


    @ElementCollection
    @CollectionTable(name = "classes", joinColumns = @JoinColumn(name = "id"))
    @Field("classes")
    private Set<String> classes;

    @ElementCollection
    @CollectionTable(name = "imports", joinColumns = @JoinColumn(name = "id"))
    @Field("imports")
    private Set<String> imports;

    @ElementCollection
    @CollectionTable(name = "properties", joinColumns = @JoinColumn(name = "id"))
    @Field("properties")
    private Set<String> properties;

    @ElementCollection
    @CollectionTable(name = "namespaces", joinColumns = @JoinColumn(name = "id"))
    @Field("namespaces")
    private Set<String> namespaces;

    @ElementCollection
    @CollectionTable(name = "individuals", joinColumns = @JoinColumn(name = "id"))
    @Field("individuals")
    private Set<String> individuals;

    @ElementCollection
    @CollectionTable(name = "collection", joinColumns = @JoinColumn(name = "id"))
    @Field("collection")
    private Set<String> collection;

    @Field
    private String createdAt = "ZonedDateTimeToDateConverter.INSTANCE";

    @Field
    private String updatedAt = "DateToZonedDateTimeConverter.INSTANCE";

    private String uri;
    private String title;
    private String description;

    public boolean equalsTsOntology(Ontology ontology) {
        return Objects.nonNull(ontology) &&
                this.getOntologyId().equalsIgnoreCase(ontology.getOntologyId());
    }

    public static <T extends ExtendedOntology> ProcessedOntology of(T ontology) {

        return ProcessedOntology.builder()
                .ontologyId(ontology.getOntologyId())
                .uri(ontology.getUri())
                .title(ontology.getTitle())
                .properties(CollectionUtils.isEmpty(ontology.getProperties()) ? Collections.emptySet() : ontology.getProperties())
                .classes(CollectionUtils.isEmpty(ontology.getClasses()) ? Collections.emptySet() : ontology.getClasses())
                .namespaces(CollectionUtils.isEmpty(ontology.getNamespaces()) ? Collections.emptySet() : ontology.getNamespaces())
                .imports(CollectionUtils.isEmpty(ontology.getImports()) ? Collections.emptySet() : ontology.getImports())
                .individuals(CollectionUtils.isEmpty(ontology.getIndividuals()) ? Collections.emptySet() : ontology.getIndividuals())
                .collection(CollectionUtils.isEmpty(ontology.getCollection()) ? Collections.emptySet() : ontology.getCollection())
                .build();
    }
}
