package eu.tib.ontologyhistory.utils;

public class SparqlQueries {

    public static final String ONDET_PREFIXES = """
            PREFIX pro: <http://purl.org/hpi/patchr#>
            PREFIX pr: <http://purl.org/ontology/prv/core#>
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            PREFIX prov: <http://www.w3.org/ns/prov#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
            \n
            
            """;

    public static final String ONDET_SELECT_CLAUSE = """
            SELECT DISTINCT
            """;

    public static final String ONDET_WHERE_CLAUSE = """
            WHERE {
              ?second_commit prov:wasRevisionOf ?first_commit .
              ?second_commit prov:generatedAtTime ?second_commit_time .
              ?second_commit rdfs:label ?second_commit_label .
              ?second_commit prov:value ?second_commit_message .
              ?second_commit prov:alternateOf ?second_commit_url .
              ?second_commit_url rdfs:seeAlso ?second_commit_graph .
              ?second_commit_operation ?second_commit_pp ?second_commit .
              ?second_commit_operation prov:atLocation ?second_commit_location .
              ?second_commit_location prov:dm ?diff .
              ?second_commit_pp rdfs:subPropertyOf ?second_commit_property .
              ?second_commit_pp rdfs:label ?second_commit_pp_label .
              
              ?first_commit prov:value ?first_commit_title .
              ?first_commit prov:generatedAtTime ?first_commit_time .
              ?first_commit rdfs:label ?first_commit_label .
              ?first_commit prov:value ?first_commit_message .
              ?first_commit prov:alternateOf ?first_commit_url .
              ?first_commit_url rdfs:seeAlso ?first_commit_graph .
              ?first_commit_operation ?first_commit_pp ?first_commit .
              ?first_commit_operation prov:atLocation ?first_commit_location .
              ?first_commit_location prov:dm ?diff .
              ?first_commit_pp rdfs:subPropertyOf ?first_commit_property .
              ?first_commit_pp rdfs:label ?first_commit_pp_label .
            
              GRAPH ?diff {
                ?s ?p ?o .
              }
              
            }
            """;

    public static final String ONDET_FILTERS_ONTOLOGY_URL = """
            FILTER(?label = ?ontologyURL) .
            """;

    public static final String ONDET_FILTERS_DATETIME_RANGE = """
            FILTER(?first_commit_time >= ?firstCommitTime^^xsd:dateTime &&
            ?second_commit_time <= ?secondCommitTime^^xsd:dateTime &&
            ?first_commit_time < ?second_commit_time) .
            """;

    public static final String ONDET_FILTERS_COMMIT_ID = """
            FILTER(str(?commit) = ?commitId) .
            """;

    public static final String GET_ALL_GRAPHS = """
            SELECT DISTINCT ?graph
            
            WHERE {
              ?commit_id prov:generatedAtTime ?first_commit_time .
              ?commit_id rdfs:label ?commit .
              ?commit_id prov:value ?message .
              ?commit_id prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?graph .
            }
            """;

    public static final String GET_GRAPH_BY_URL = """
            
            SELECT DISTINCT ?graph
            
            WHERE {
              ?commit_id prov:generatedAtTime ?first_commit_time .
              ?commit_id rdfs:label ?commit .
              ?commit_id prov:value ?message .
              ?commit_id prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?graph .
            
              FILTER(?graph = ?ontologyURL) .
            }
            """;

    public static final String WHOLE_ONTOLOGY_TIMELINE = """
            
            SELECT DISTINCT ?commit_label ?message ?first_commit_time
            
            WHERE {
            
              ?commit_id prov:generatedAtTime ?first_commit_time .
              ?commit_id rdfs:label ?commit_label .
              ?commit_id prov:value ?message .
              ?commit_id prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?label .
            
              FILTER(?label = ?ontologyURL) .
            }
            """;

    public static final String WHOLE_ONTOLOGY_TIMELINE_MESSAGE = """
            
            SELECT DISTINCT ?pp_label ?p ?o ?first_commit_time
            
            WHERE {
            
              ?second_commit prov:wasRevisionOf ?first_commit .
              ?second_commit prov:generatedAtTime ?second_commit_time .
              ?first_commit prov:value ?first_commit_title .
              ?first_commit prov:generatedAtTime ?first_commit_time .
              ?operation ?pp ?second_commit .
              ?pp rdfs:subPropertyOf ?property .
              ?pp rdfs:label ?pp_label .
              ?operation prov:atLocation ?location .
              ?location prov:dm ?diff .
              ?second_commit prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?label .
            
              GRAPH ?diff {
                ?resourceArg ?p ?o .
              }
            
              FILTER(?label = ?ontologyURL) .
              FILTER(?first_commit_time >= ?firstCommitTime^^xsd:dateTime &&
                     ?second_commit_time <= ?secondCommitTime^^xsd:dateTime &&
                     ?first_commit_time < ?second_commit_time) .
            }
            """;

    public static final String WHOLE_ONTOLOGY_TIMELINE_ELEMENT = """
            
            SELECT DISTINCT ?pp_label ?s ?p ?o
            
            WHERE {
            
              ?commit_id prov:wasRevisionOf ?first_commit .
              ?commit_id prov:generatedAtTime ?second_commit_time .
              ?commit_id rdfs:label ?commit .
              ?commit_id prov:value ?message .
              ?commit_id prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?label .
              ?operation ?pp ?commit_id .
              ?pp rdfs:subPropertyOf ?property .
              ?pp rdfs:label ?pp_label .
              ?operation prov:atLocation ?location .
              ?location prov:dm ?diff .
            
              GRAPH ?diff {
                ?s ?p ?o .
              }
            
              FILTER(str(?commit) = ?commitId) .
            
            }
            """;

    public static final String ALL_DISTINCT_OPERATIONS = """
            
            SELECT DISTINCT ?function
            
            WHERE {
            
              ?first_commit prov:wasRevisionOf ?second_commit .
              ?first_commit prov:generatedAtTime ?first_commit_time .
              ?first_commit prov:value ?first_commit_title .
              ?operation ?function ?first_commit .
              ?function rdfs:subPropertyOf ?property .
              ?operation prov:atLocation ?location .
              ?location prov:dm ?diff .
              ?commit_id prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?label .
            
              GRAPH ?diff {
                ?s ?p ?o .
              }
            
              FILTER(?label = ?ontologyURL) .
            }
            """;

    public static final String DATA_RELATED_TO_OPERATION = """
            
             SELECT DISTINCT ?s
            
             WHERE {
            
               ?operation prov:atLocation ?location .
               ?location prov:dm ?diff .
               ?ontology_url rdfs:seeAlso ?label .
            
               GRAPH ?diff {
                 ?s ?p ?o .
               }
            
               FILTER(?label = ?ontologyURL) .
             }
            """;

    public static final String DATA_BETWEEN_TWO_DATES = """
            
            SELECT DISTINCT ?pp_label ?s ?p ?o
            
            WHERE {
              ?second_commit prov:wasRevisionOf ?first_commit .
              ?second_commit prov:generatedAtTime ?second_commit_time .
              ?first_commit prov:value ?first_commit_title .
              ?first_commit prov:generatedAtTime ?first_commit_time .
              ?operation ?pp ?second_commit .
              ?pp rdfs:subPropertyOf ?property .
              ?pp rdfs:label ?pp_label .
              ?operation prov:atLocation ?location .
              ?location prov:dm ?diff .
              ?second_commit prov:alternateOf ?ontology_url .
              ?ontology_url rdfs:seeAlso ?label .
            
              GRAPH ?diff {
                ?s ?p ?o .
              }
            
              FILTER(?label = ?ontologyURL) .
              FILTER(?second_commit_time >= ?startDatetimeArg^^xsd:dateTime &&
                     ?second_commit_time <= ?endDatetimeArg^^xsd:dateTime) .
            }
            """;

}
