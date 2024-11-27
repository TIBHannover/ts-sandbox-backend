package eu.tib.tiva.service;

import eu.tib.tiva.model.eurostat.ChartObj;
import eu.tib.tiva.utils.Queries;
import eu.tib.tiva.model.eurostat.EurostatSparqlField;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class EurostatService {

    private static final String GRAPHDB_ENDPOINT_URL = "http://sc3onto01.develop.service.tib.eu:7200/repositories/default";

    private static String generateValuesClause(String field, List<String> inputs) {
        StringBuilder values = new StringBuilder("VALUES ?").append(field).append(" {");
        for (String input : inputs) {
            values.append(input).append(System.lineSeparator());
        }
        values.append(" }").append(System.lineSeparator());
        return values.toString();
    }

    private String getQueryByFields(Set<String> fields) {
        String prefixes = Queries.EUROSTAT_PREFIXES;

        StringBuilder select = new StringBuilder("SELECT DISTINCT");
        StringBuilder where = new StringBuilder(Queries.EUROSTAT_WHERE_CLAUSE);
        for (String field : fields) {
            switch (EurostatSparqlField.valueOf(field.toUpperCase())) {
                case REPORTER:
                    select.append(" ?reporter");
                    where.append(" ?data <https://schema.coypu.org/global#hasReporter> ?reporter .");
                    break;
                case PARTNER:
                    select.append(" ?partner");
                    where.append(" ?data <https://schema.coypu.org/global#hasPartner> ?partner .");
                    break;
                case PRODUCT:
                    select.append(" ?product");
                    where.append(" ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .");
                    break;
                case FLOW:
                    select.append(" ?flow");
                    where.append(" ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .");
                    break;
                case VALUE:
                    select.append(" ?value");
                    where.append("?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .")
                            .append(" ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .");
                    break;
                case YEAR:
                    select.append(" (SUBSTR(?year, 0, spif:indexOf(?year, '-') + 1) AS ?yearTrimmed)");
                    where.append("?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .")
                            .append(" ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .");
                    break;
                default:
                    log.warn("Blank field or field outside of switch getQueryByFields() function was received");
            }
        }

        return prefixes + select + where + "\n}";
    }

    public List<String> getListEurostat(Set<String> fields) {
        val objects = new ArrayList<String>();

        String query = getQueryByFields(fields);

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(query);

        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                for (String field : fields) {
                    String key = "";
                    if (solution.contains(field)) {
                        if (field.equals(EurostatSparqlField.FLOW.getValue()) || field.equals(EurostatSparqlField.VALUE.getValue())
                                || field.equals(EurostatSparqlField.YEAR_TRIMMED.getValue()) || field.equals(EurostatSparqlField.PRODUCT.getValue())) {
                            key = solution.get(field).toString();
                        } else {
                            key += solution.get(field).asResource().getLocalName();
                        }
                    }
                    objects.add(key);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return objects;
    }

    public Map<String, List<ChartObj>> getRepYearSum(String reporter, String year) {
        val objects = new HashMap<String, List<ChartObj>>();
        val obj = new ArrayList<ChartObj>();

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(Queries.EUROSTAT_REP_YEAR_SUM);
        graphQuery.setLiteral("reporter_param", reporter);
        graphQuery.setLiteral("year_param", year);
        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get(EurostatSparqlField.YEAR_TRIMMED.getValue()).asLiteral().getValue().toString();
                val sumVal = solution.get(EurostatSparqlField.VALUES_SUM.getValue()).asLiteral().getValue().toString();
                obj.add(new ChartObj(yearTrimmed, sumVal));
                objects.put("data", obj);
            }
        }
        return objects;
    }

    public Map<String, String> getReporterPartnerProducts(List<String> reporters, String startDate, String endDate, List<String> partners, String flow, List<String> products) {
        val objects = new HashMap<String, String>();

        String reporterValues = generateValuesClause(EurostatSparqlField.REPORTER.getValue(), reporters.stream().map(r -> "atold:" + r).toList());
        String partnerValues = generateValuesClause(EurostatSparqlField.PARTNER.getValue(), partners.stream().map(r -> "atold:" + r).toList());
        String productValues = generateValuesClause(EurostatSparqlField.PRODUCT.getValue(), products.stream().map(r -> "coypu_hs:" + r).toList());

        String query = Queries.EUROSTAT_SUM_VALUES_BY_REPORTER_PARTNER_PRODUCT
                .replace("[[REPORTER_VALUES_PLACEHOLDER]]", reporterValues)
                .replace("[[PARTNER_VALUES_PLACEHOLDER]]", partnerValues)
                .replace("[[PRODUCT_VALUES_PLACEHOLDER]]", productValues);

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(query);
        graphQuery.setLiteral("start_date_param", startDate);
        graphQuery.setLiteral("end_date_param", endDate);
        graphQuery.setLiteral("flow_param", flow);

        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val productVal = solution.get(EurostatSparqlField.PRODUCT.getValue()).asResource().toString();
                val reporterVal = solution.get(EurostatSparqlField.REPORTER.getValue()).asResource().getLocalName();
                val partnerVal = solution.get(EurostatSparqlField.PARTNER.getValue()).asResource().getLocalName();
                val sumVal = solution.get(EurostatSparqlField.VALUES_SUM.getValue()).asLiteral().getValue().toString();

                String key = productVal.substring(productVal.lastIndexOf('/') + 1) + "_" + reporterVal + "_" + partnerVal;
                objects.put(key, sumVal);
            }
        }

        return objects;
    }

    public Map<String, List<ChartObj>> getYearRange(
            List<String> reporters, String startDate, String endDate, List<String> partners, String flow, List<String> products) {

        val objects = new HashMap<String, List<ChartObj>>();

        String reporterValues = generateValuesClause(EurostatSparqlField.REPORTER.getValue(), reporters.stream().map(r -> "atold:" + r).toList());
        String partnerValues = generateValuesClause(EurostatSparqlField.PARTNER.getValue(), partners.stream().map(r -> "atold:" + r).toList());
        String productValues = generateValuesClause(EurostatSparqlField.PRODUCT.getValue(), products.stream().map(r -> "coypu_hs:" + r).toList());

        String query = Queries.EUROSTAT_SUM_VALUES_BY_REPORTER_PARTNER_YEAR
                .replace("[[REPORTER_VALUES_PLACEHOLDER]]", reporterValues)
                .replace("[[PARTNER_VALUES_PLACEHOLDER]]", partnerValues)
                .replace("[[PRODUCT_VALUES_PLACEHOLDER]]", productValues);

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(query);
        graphQuery.setLiteral("start_date_param", startDate);
        graphQuery.setLiteral("end_date_param", endDate);
        graphQuery.setLiteral("flow_param", flow);

        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get(EurostatSparqlField.YEAR_TRIMMED.getValue()).asLiteral().getValue().toString();
                val sumVal = solution.get(EurostatSparqlField.VALUES_SUM.getValue()).asLiteral().getValue().toString();
                val reporterVal = solution.get(EurostatSparqlField.REPORTER.getValue()).asResource().getLocalName();
                val partnerVal = solution.get(EurostatSparqlField.PARTNER.getValue()).asResource().getLocalName();

                String key = reporterVal + "_" + partnerVal;

                val obj = objects.getOrDefault(key, new ArrayList<>());
                obj.add(new ChartObj(yearTrimmed, sumVal));
                objects.put(key, obj);
            }
        }

        return objects;
    }

    public List<ChartObj> getGDPInformation(List<String> geoEntities, String unit) {
        val geoEntitiesValues = generateValuesClause(EurostatSparqlField.GEO.getValue(), geoEntities.stream().map(r -> "atold:" + r).toList());

        String query = Queries.EUROSTAT_REAL_GDP_GROWTH_RATE
                .replace("[[VALUES_PLACEHOLDER]]", geoEntitiesValues);

        val sparqlString = new ParameterizedSparqlString();
        sparqlString.setCommandText(query);
        sparqlString.setLiteral("unit_param", unit.concat("^^xsd:string "));

        val objects = new ArrayList<ChartObj>();
        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(sparqlString.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get(EurostatSparqlField.YEAR_TRIMMED.getValue()).asLiteral().getValue().toString();
                val sumVal = solution.get(EurostatSparqlField.VALUES_SUM.getValue()).asLiteral().getValue().toString();

                objects.add(new ChartObj(yearTrimmed, sumVal));
            }
        }
        return objects;
    }

    public List<String> getGeoCountries() {
        val sparqlString = new ParameterizedSparqlString();
        sparqlString.setCommandText(Queries.EUROSTAT_REAL_GDP_DISTINCT_GEOS);

        val geos = new ArrayList<String>();
        try (QueryExecutionHTTP q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(sparqlString.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val geo = solution.get(EurostatSparqlField.GEO_TRIMMED.getValue()).asLiteral().toString();
                geos.add(geo);
            }
        }
        return geos;
    }

    public List<ChartObj> getAnnualEnteprise(List<String> geoEntities, String indicator) {
        val geoEntitiesValues = generateValuesClause(EurostatSparqlField.GEO.getValue(), geoEntities.stream().map(r -> "atold:" + r).toList());

        String query = Queries.EUROSTAT_ANNUAL_ENTERPRISE_STATISTICS
                .replace("[[VALUES_PLACEHOLDER]]", geoEntitiesValues);

        val sparqlString = new ParameterizedSparqlString();
        sparqlString.setCommandText(query);
        sparqlString.setLiteral("indicator_param", indicator.concat("^^xsd:string "));

        val objects = new ArrayList<ChartObj>();
        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(sparqlString.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get(EurostatSparqlField.YEAR_TRIMMED.getValue()).asLiteral().getValue().toString();
                val sumVal = solution.get(EurostatSparqlField.VALUES_SUM.getValue()).asLiteral().getValue().toString();

                objects.add(new ChartObj(yearTrimmed, sumVal));
            }
        }
        return objects;
    }

    public List<String> getAnnualEntepriseGeos() {
        val sparqlString = new ParameterizedSparqlString();
        sparqlString.setCommandText(Queries.EUROSTAT_ANNUAL_ENTERPRISE_STATISTICS_DISTINCT_GEOS);

        val geos = new ArrayList<String>();
        try (QueryExecutionHTTP q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(sparqlString.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val geo = solution.get(EurostatSparqlField.GEO_TRIMMED.getValue()).asLiteral().toString();
                geos.add(geo);
            }
        }
        return geos;
    }

    public List<ChartObj> getSoldProduction(List<String> geoEntities, String indicator) {
        val geoEntitiesValues = generateValuesClause(EurostatSparqlField.GEO.getValue(), geoEntities.stream().map(r -> "atold:" + r).toList());

        String query = Queries.EUROSTAT_SOLD_PRODUCTION
                .replace("[[VALUES_PLACEHOLDER]]", geoEntitiesValues);

        val sparqlString = new ParameterizedSparqlString();
        sparqlString.setCommandText(query);
        sparqlString.setLiteral("indicator_param", indicator.concat("^^xsd:string "));

        val objects = new ArrayList<ChartObj>();
        try (QueryExecution q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(sparqlString.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get(EurostatSparqlField.YEAR_TRIMMED.getValue()).asLiteral().getValue().toString();
                val sumVal = solution.get(EurostatSparqlField.VALUES_SUM.getValue()).asLiteral().getValue().toString();

                objects.add(new ChartObj(yearTrimmed, sumVal));
            }
        }
        return objects;
    }

    public List<String> getSoldProductionDecl() {
        val sparqlString = new ParameterizedSparqlString();
        sparqlString.setCommandText(Queries.EUROSTAT_SOLD_PRODUCTION_DISTINCT_GEOS);

        val geos = new ArrayList<String>();
        try (QueryExecutionHTTP q = QueryExecutionHTTP.service(GRAPHDB_ENDPOINT_URL)
                .query(sparqlString.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val geo = solution.get(EurostatSparqlField.GEO_TRIMMED.getValue()).asLiteral().toString();
                geos.add(geo);
            }
        }
        return geos;
    }
}
