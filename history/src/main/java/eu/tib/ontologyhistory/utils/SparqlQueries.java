package eu.tib.ontologyhistory.utils;

public class SparqlQueries {
    public static final String GET_ALL_GRAPHS = """
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

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
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

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
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        
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
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

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
          ?commit_id prov:alternateOf ?ontology_url .
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
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

        SELECT ?pp_label ?s ?p ?o

        WHERE {

          ?commit_id prov:generatedAtTime ?first_commit_time .
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
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

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
         PREFIX pro: <http://purl.org/hpi/patchr#>
         PREFIX pr: <http://purl.org/ontology/prv/core#>
         PREFIX owl: <http://www.w3.org/2002/07/owl#>
         PREFIX prov: <http://www.w3.org/ns/prov#>
         PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
         PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    
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
        PREFIX pro: <http://purl.org/hpi/patchr#>
        PREFIX pr: <http://purl.org/ontology/prv/core#>
        PREFIX owl: <http://www.w3.org/2002/07/owl#>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

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
          ?commit_id prov:alternateOf ?ontology_url .
          ?ontology_url rdfs:seeAlso ?label .
        
          GRAPH ?diff {
            ?s ?p ?o .
          }
        
          FILTER(?label = ?ontologyURL) .
          FILTER(?first_commit_time >= ?firstCommitTime^^xsd:dateTime &&
                 ?second_commit_time <= ?secondCommitTime^^xsd:dateTime &&
                 ?first_commit_time < ?second_commit_time) .
        }
        """;

}
