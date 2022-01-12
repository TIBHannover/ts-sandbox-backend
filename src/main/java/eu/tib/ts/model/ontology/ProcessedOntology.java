package eu.tib.ts.model.ontology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_ontology")
public class ProcessedOntology implements ExtendedOntology {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ontology_id_generator")
    @SequenceGenerator(name = "ontology_id_generator", sequenceName = "ont_id_seq", allocationSize = 1)
    private long id;

    @Column(name = "ontology_id", unique = true, nullable = false)
    private String ontologyId;

    @ElementCollection
    @CollectionTable(name = "classes", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "classes")
    private Set<String> classes;

    @ElementCollection
    @CollectionTable(name = "imports", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "imports")
    private Set<String> imports;

    @ElementCollection
    @CollectionTable(name = "properties", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "properties")
    private Set<String> properties;

    @ElementCollection
    @CollectionTable(name = "namespaces", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "namespaces")
    private Set<String> namespaces;

    @ElementCollection
    @CollectionTable(name = "collection", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "collection")
    private Set<String> collection;

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", name = "created_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", name = "update_at")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    private String uri;

    public boolean equalsTsOntology(Ontology ontology) {
        return Objects.nonNull(ontology) &&
            this.getOntologyId().equals(ontology.getOntologyId());
    }

    public static <T extends ExtendedOntology> ProcessedOntology of(T ontology) {
        return ProcessedOntology.builder()
            .ontologyId(ontology.getOntologyId())
            .uri(ontology.getUri())
            .properties(ontology.getProperties())
            .classes(ontology.getClasses())
            .namespaces(ontology.getNamespaces())
            .imports(ontology.getImports())
            .collection(ontology.getCollection())
            .build();
    }
}
