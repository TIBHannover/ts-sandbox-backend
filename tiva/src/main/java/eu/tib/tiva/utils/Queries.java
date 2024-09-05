package eu.tib.tiva.utils;

public class Queries {

    public static String EUROSTAT_PREFIXES =
            """
                    PREFIX spif: <http://spinrdf.org/spif#>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX dct: <http://purl.org/dc/terms/>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX dcat: <http://www.w3.org/ns/dcat#>
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    """;

    public static String EUROSTAT_WHERE_CLAUSE =
            """
                    \nWHERE {
                    \n?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                    \n?dataset <http://purl.org/dc/terms/title> ?title .
                    \n?dataset <https://schema.coypu.org/global#hasData>  ?data .
                    \n?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                    """;

    public static String EUROSTAT =
            """
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                                PREFIX dct: <http://purl.org/dc/terms/>
                                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                                PREFIX dcat: <http://www.w3.org/ns/dcat#>
                                PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                                SELECT DISTINCT ?dataset ?title ?reporter ?partner ?flow ?product ?value ?year
                                WHERE
                                 {
                                    ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                                    ?dataset <http://purl.org/dc/terms/title> ?title .
                                    ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                                    ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                                    ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                                    ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                                    ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                                    ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                                    ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                                    ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                                    ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                                 }
                    """;

    public static String EUROSTAT_REP_YEAR_SUM =
            """
                    PREFIX spif: <http://spinrdf.org/spif#>
                                            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                                                    PREFIX dct: <http://purl.org/dc/terms/>
                                                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                                                    PREFIX dcat: <http://www.w3.org/ns/dcat#>
                                                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                                                    PREFIX atold: <http://publications.europa.eu/resource/authority/>
                            SELECT DISTINCT ?reporter (SUBSTR(?year, 0, spif:lastIndexOf(?year, "^")) AS ?yearTrimmed)
                            (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?sumVal)
                                                    WHERE
                                                     {
                                                        ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                                                        ?dataset <http://purl.org/dc/terms/title> ?title .
                                                        ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                                                        ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                                                        ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                                                        ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                                                        ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                                                        ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                                                        ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                                                        ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                                                        ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                    
                                FILTER(CONTAINS(STR(?reporter), ?repArg)) .
                                FILTER(CONTAINS(?year, ?yearArg)) .
                            }
                            GROUP BY ?reporter ?year
                    """;

    public static String EUROSTAT_YEAR_RANGE =
            """
                    PREFIX spif: <http://spinrdf.org/spif#>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX dct: <http://purl.org/dc/terms/>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX dcat: <http://www.w3.org/ns/dcat#>
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    PREFIX atold: <http://publications.europa.eu/resource/authority/>
                    
                    SELECT DISTINCT ?reporter
                                    (STRDT(CONCAT(SUBSTR(?year, 1, 7), "-01"), xsd:date) AS ?yearTrimmed)\s
                                    ?year
                                    (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?sumVal)
                    WHERE {
                      ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                      ?dataset <http://purl.org/dc/terms/title> ?title .
                      ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                      ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                      ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                      ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                      ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                      ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                      ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                      ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                      ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                    
                      # Filtering for specific reporter and year containing '1996'
                      FILTER(CONTAINS(STR(?reporter), 'AT')) .
                    
                      # Convert and filter for dates within a certain range
                      FILTER(STRDT(CONCAT(SUBSTR(?year, 1, 7), "-01"), xsd:date) >= ?startDate^^xsd:date &&
                             STRDT(CONCAT(SUBSTR(?year, 1, 7), "-01"), xsd:date) <= ?endDate^^xsd:date)
                    }
                    GROUP BY ?reporter ?year
                    """;
}
