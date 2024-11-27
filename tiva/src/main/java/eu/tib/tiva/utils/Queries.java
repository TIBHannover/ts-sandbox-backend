package eu.tib.tiva.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Queries {

    public static final String EUROSTAT_PREFIXES =
            """
                    PREFIX spif: <http://spinrdf.org/spif#>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX dct: <http://purl.org/dc/terms/>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX dcat: <http://www.w3.org/ns/dcat#>
                    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
                    PREFIX atold: <http://publications.europa.eu/resource/authority/country/>
                    PREFIX coypu_hs: <https://data.coypu.org/classification/hs_2012/>
                    
                    """;

    public static final String EUROSTAT_WHERE_CLAUSE =
            """
                    \n
                    WHERE {
                    ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                    ?dataset <http://purl.org/dc/terms/title> ?title .
                    ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                    ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                    """;

    public static final String EUROSTAT_FULL_WHERE_CLAUSE =
            """
                            ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                            ?dataset <http://purl.org/dc/terms/title> ?title .
                            ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                            ?data   <https://schema.coypu.org/global#hasIndicator>  ?indicator .
                            ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                            ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                            ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                            ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                            ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                            ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                            ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                            ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                            FILTER(CONTAINS(?indicator,"VALUE_IN_EUROS^^xsd:string"))
                    """;

    public static final String EUROSTAT =
            EUROSTAT_PREFIXES +
                    """
                                        SELECT DISTINCT ?dataset ?title ?reporter ?partner ?flow ?product ?value ?year
                                        WHERE
                                         {
                                            ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                                            ?dataset <http://purl.org/dc/terms/title> ?title .
                                            ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                                            ?data   <https://schema.coypu.org/global#hasIndicator>  ?indicator .
                                            ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                                            ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                                            ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                                            ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                                            ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                                            ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                                            ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                                            ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                                            FILTER(CONTAINS(?indicator,"VALUE_IN_EUROS^^xsd:string"))
                                         }
                            """;

    public static final String EUROSTAT_REP_YEAR_SUM =
            EUROSTAT_PREFIXES +
                    """
                                    SELECT DISTINCT ?reporter (SUBSTR(?year, 0, spif:lastIndexOf(?year, "^")) AS ?year_trimmed)
                                    (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?values_sum)
                                                            WHERE
                                                             {
                                                                ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                                                                ?dataset <http://purl.org/dc/terms/title> ?title .
                                                                ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                                                                ?data   <https://schema.coypu.org/global#hasIndicator>  ?indicator .
                                                                ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                                                                ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                                                                ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                                                                ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                                                                ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                                                                ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                                                                ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                                                                ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                                                                FILTER(CONTAINS(?indicator,"VALUE_IN_EUROS^^xsd:string"))
                            
                                        FILTER(CONTAINS(STR(?reporter), ?reporter_param)) .
                                        FILTER(CONTAINS(?year, ?year_param)) .
                                    }
                                    GROUP BY ?reporter ?year
                            """;

    public static final String EUROSTAT_SUM_VALUES_BY_REPORTER_PARTNER_PRODUCT =
            EUROSTAT_PREFIXES +
                    """
                            SELECT DISTINCT ?reporter ?partner ?product
                            (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?values_sum)
                            WHERE {
                            
                            [[REPORTER_VALUES_PLACEHOLDER]]
                            
                            [[PARTNER_VALUES_PLACEHOLDER]]
                            
                            [[PRODUCT_VALUES_PLACEHOLDER]]
                            
                                     ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                                     ?dataset <http://purl.org/dc/terms/title> ?title .
                                     ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                                     ?data   <https://schema.coypu.org/global#hasIndicator>  ?indicator .
                                     ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                                     ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                                     ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                                     ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                                     ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                                     ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                                     ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                                     ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                            
                            FILTER(CONTAINS(?indicator,"VALUE_IN_EUROS^^xsd:string"))
                            FILTER(STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) >= ?start_date_param^^xsd:date &&
                                   STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) <= ?end_date_param^^xsd:date) .
                            FILTER(CONTAINS(STR(?flow), ?flow_param)) .
                            
                            } GROUP BY ?product ?reporter ?partner
                            
                            """;

    public static final String EUROSTAT_SUM_VALUES_BY_REPORTER_PARTNER_YEAR =
            EUROSTAT_PREFIXES +
                    """
                            SELECT DISTINCT ?reporter (STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) AS ?year_trimmed)
                            (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?values_sum)
                            ?partner
                            
                            WHERE {
                            
                             [[REPORTER_VALUES_PLACEHOLDER]]
                            
                             [[PARTNER_VALUES_PLACEHOLDER]]
                            
                             [[PRODUCT_VALUES_PLACEHOLDER]]
                            
                                     ?dataset rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                                     ?dataset <http://purl.org/dc/terms/title> ?title .
                                     ?dataset <https://schema.coypu.org/global#hasData>  ?data .
                                     ?data   <https://schema.coypu.org/global#hasIndicator>  ?indicator .
                                     ?data <http://publications.europa.eu/ontology/cdm#has_frequency>  <http://publications.europa.eu/resource/authority/frequency/MONTHLY> .
                                     ?data <https://schema.coypu.org/global#hasReporter> ?reporter .
                                     ?data <https://schema.coypu.org/global#hasPartner> ?partner .
                                     ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .
                                     ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .
                                     ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .
                                     ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .
                                     ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                            
                                     FILTER(CONTAINS(?indicator,"VALUE_IN_EUROS^^xsd:string"))
                             FILTER(STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) >= ?start_date_param^^xsd:date &&
                                    STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) <= ?end_date_param^^xsd:date) .
                             FILTER(CONTAINS(STR(?flow), ?flow_param)) .
                            
                             } GROUP BY ?reporter ?year ?partner
                            
                            """;


    public static final String EUROSTAT_REAL_GDP_FULL_WHERE_CLAUSE =
            """
                    
                            ?s rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                            ?s <http://purl.org/dc/terms/title> ?title .
                            ?s dct:identifier   ?identifier .
                            ?s <http://www.w3.org/ns/dcat#landingPage> ?loadingPage .
                            ?s <http://publications.europa.eu/ontology/cdm/cmr#lastModificationDate> ?lastModificationDate .
                            ?s <http://publications.europa.eu/ontology/cdm#date_modified>    ?dateModified .
                            ?s <https://schema.coypu.org/global#hasData> ?data .
                            ?data <http://publications.europa.eu/ontology/cdm#has_frequency> ?frequency .
                            ?data <https://schema.coypu.org/global#hasUnit> ?unit .
                            ?data <https://schema.coypu.org/global#hasIndicator> ?indicator .
                            ?data <https://schema.coypu.org/global#hasGeopoliticalEntity> ?geo .
                            ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?ns_value .
                            ?ns_value <https://schema.coypu.org/global#hasValue> ?coy_value .
                            ?ns_value <http://publications.europa.eu/ontology/cdm#year> ?year .
                    
                    """;

    public static final String EUROSTAT_REAL_GDP_DISTINCT_GEOS =
            EUROSTAT_PREFIXES +
                    """
                        select distinct (STRAFTER(str(?geo), "http://publications.europa.eu/resource/authority/country/") as ?geo_trimmed)
                        where {
                    """
                    +
                    EUROSTAT_REAL_GDP_FULL_WHERE_CLAUSE
                    +
                    """
                        }
                    """;

    public static final String EUROSTAT_REAL_GDP_GROWTH_RATE =
            EUROSTAT_PREFIXES +
                    """
                            #This SPARQL query retrieves all data from tec00115 dataset

                            select distinct
                            (SUM(xsd:decimal(SUBSTR(?coy_value, 0, spif:lastIndexOf(?coy_value, "^")))) AS ?values_sum)
                            (SUBSTR(?year, 1, 4) AS ?year_trimmed)
                            ?unit

                            where {

                            [[VALUES_PLACEHOLDER]] .
                    """
                    +
                    EUROSTAT_REAL_GDP_FULL_WHERE_CLAUSE
                    +
                    """
                        filter(STR(?unit) = ?unit_param^^xsd:string) .
                            }
                        group by ?year ?unit
                    """;

    public static final String EUROSTAT_ANNUAL_ENTERPROSE_STATISTICS_FULL_WHERE_CLAUSE =
            """
                
                ?s rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                ?s <http://purl.org/dc/terms/title> ?title .
                ?s dct:identifier   ?identifier .
                ?s <http://www.w3.org/ns/dcat#landingPage> ?loadingPage .
                ?s <http://publications.europa.eu/ontology/cdmplus#dateEnd> ?dateEnd .
                ?s <http://publications.europa.eu/ontology/cdm/cmr#lastModificationDate> ?lastModificationDate .
                ?s <http://publications.europa.eu/ontology/cdm#date_modified> ?dateModified .
                ?s <https://schema.coypu.org/global#hasData> ?data .
                ?data <http://publications.europa.eu/ontology/cdm#has_frequency> ?frequency .
                ?data <https://schema.coypu.org/global#hasGeopoliticalEntity>  ?geo .
                ?data <https://schema.coypu.org/vtf#hasIndustryCode> ?industryCode .
                ?data <https://schema.coypu.org/global#hasIndicator>  ?indicator .
                ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?ns_value .
                ?ns_value <https://schema.coypu.org/global#hasValue> ?coy_value .
                ?ns_value <http://publications.europa.eu/ontology/cdm#year>  ?year .
                
            """;

    public static final String EUROSTAT_ANNUAL_ENTERPRISE_STATISTICS_DISTINCT_GEOS =
            EUROSTAT_PREFIXES +
            """
            select distinct (STRAFTER(str(?geo), "http://publications.europa.eu/resource/authority/country/") as ?geo_trimmed)
            where {        
            """
            +
                    EUROSTAT_ANNUAL_ENTERPROSE_STATISTICS_FULL_WHERE_CLAUSE
            +
            """
                }
            """;

    public static final String EUROSTAT_ANNUAL_ENTERPRISE_STATISTICS =
            EUROSTAT_PREFIXES +
                    """
                        #This SPARQL query retrieves all data from sbs_na_sca_r2 dataset
                        
                        select distinct
                        (SUM(xsd:decimal(SUBSTR(?coy_value, 0, spif:lastIndexOf(?coy_value, "^")))) AS ?values_sum)
                        (SUBSTR(?year, 1, 4) AS ?year_trimmed)
                        (SUBSTR(?indicator, 1, 6) as ?indicator_trimmed)
                        
                        where {
                        
                        [[VALUES_PLACEHOLDER]] .
                    """
                    +
                    EUROSTAT_ANNUAL_ENTERPROSE_STATISTICS_FULL_WHERE_CLAUSE
                    +
                    """
                        filter(STR(?indicator) = ?indicator_param^^xsd:string) .
                        }
                        
                        group by ?year ?indicator
                    """;

    public static final String EUROSTAT_SOLD_PRODUCTION_FULL_WHERE_CLAUSE =
            """
            
                ?s rdf:type <http://www.w3.org/ns/dcat#Dataset> .
                ?s <http://purl.org/dc/terms/title> ?title .
                ?s dct:identifier   ?identifier .
                ?s <http://www.w3.org/ns/dcat#landingPage> ?loadingPage .
                ?s <http://publications.europa.eu/ontology/cdm/cmr#lastModificationDate> ?lastModificationDate .
                ?s <http://publications.europa.eu/ontology/cdm#date_modified>    ?dateModified .
                ?s <https://schema.coypu.org/global#hasData> ?data .
                ?data <http://publications.europa.eu/ontology/cdm#has_frequency> ?frequency .
                ?data <https://schema.coypu.org/global#hasDecl> ?geo .
                ?data <https://schema.coypu.org/global#hasIndicator> ?indicator .
                ?data <https://schema.coypu.org/global#hasPrcCode>  ?prc_code .
                ?data <http://www.w3.org/2006/vcard/ns#hasValue> ?ns_value .
                ?ns_value <https://schema.coypu.org/global#hasValue> ?coy_value .
                ?ns_value <http://publications.europa.eu/ontology/cdm#year> ?year .                    
            
            """;

    public static final String EUROSTAT_SOLD_PRODUCTION_DISTINCT_GEOS =
            EUROSTAT_PREFIXES +
                    """
                        select distinct (STRAFTER(str(?geo), "http://publications.europa.eu/resource/authority/country/") as ?geo_trimmed)
                        where {
                    """
                    +
                    EUROSTAT_SOLD_PRODUCTION_FULL_WHERE_CLAUSE
                    +
                    """
                        }
                    """;

    public static final String EUROSTAT_SOLD_PRODUCTION =
            EUROSTAT_PREFIXES +
                    """
                        #This SPARQL query retrieves all data from ds-056120 dataset
                        
                        select distinct
                        (SUM(xsd:decimal(SUBSTR(?coy_value, 0, spif:lastIndexOf(?coy_value, "^")))) AS ?values_sum)
                        (SUBSTR(?year, 1, 4) AS ?year_trimmed)
                        (SUBSTR(?indicator, 1, 6) as ?indicator_trimmed)
                        
                        where {
                        
                        [[VALUES_PLACEHOLDER]] .
                    """
                    +
                    EUROSTAT_SOLD_PRODUCTION_FULL_WHERE_CLAUSE
                    +
                    """
                        filter(STR(?indicator) = ?indicator_param^^xsd:string) .
                        }
                        group by ?year ?indicator
                    """;
}
