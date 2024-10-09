package eu.tib.tiva.controller;

import eu.tib.tiva.controller.assembler.GrossExportsByOriginOfValueAddedAndFinalDestinationAssembler;
import eu.tib.tiva.controller.assembler.OriginOfValueAddedInGrossImportsAssembler;
import eu.tib.tiva.controller.dto.GrossExportByOriginOfValueAddedAndFinalDestinationModel;
import eu.tib.tiva.controller.dto.OriginOfValueAddedInFinalDemandModel;
import eu.tib.tiva.controller.dto.OriginOfValueAddedInGrossImportsModel;
import eu.tib.tiva.controller.dto.ValueAndTradeFlowModel;
import eu.tib.tiva.controller.assembler.OriginOfValueAddedInFinalDemandAssembler;
import eu.tib.tiva.controller.assembler.TradeLocationCodeModelAssembler;
import eu.tib.tiva.model.*;
import eu.tib.tiva.service.ValueAndTradeFlowService;
import eu.tib.tiva.utils.HttpUtils;
import eu.tib.tiva.utils.PageUtils;
import eu.tib.tiva.utils.Queries;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.*;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("api/tiva")
public class ValueAndTradeFlowController {
    private final ValueAndTradeFlowService valueAndTradeFlowService;
    private final PagedResourcesAssembler<ValueAndTradeFlowCode> countryCodePagedResourcesAssembler;
    private final TradeLocationCodeModelAssembler tradeLocationCodeModelAssembler;
    private final PagedResourcesAssembler<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandPagedResourcesAssembler;
    private final OriginOfValueAddedInFinalDemandAssembler originOfValueAddedInFinalDemandAssembler;

    private final PagedResourcesAssembler<OriginOfValueAddedInGrossImports> originOfValueAddedInGrossImportsPagedResourcesAssembler;

    private final OriginOfValueAddedInGrossImportsAssembler originOfValueAddedInGrossImportsAssembler;

    private final PagedResourcesAssembler<GrossExportByOriginOfValueAddedAndFinalDestinations>
            grossExportByOriginOfValueAddedAndFinalDestinationsPagedResourcesAssembler;

    private final GrossExportsByOriginOfValueAddedAndFinalDestinationAssembler grossExportsByOriginOfValueAddedAndFinalDestinationAssembler;
    private final String sparqlEndPoint = "https://tiva.coypu.org/tiva";

    @Autowired
    public ValueAndTradeFlowController(
            ValueAndTradeFlowService valueAndTradeFlowService,
            PagedResourcesAssembler<ValueAndTradeFlowCode> countryCodePagedResourcesAssembler,
            PagedResourcesAssembler<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandPagedResourcesAssembler,
            TradeLocationCodeModelAssembler tradeLocationCodeModelAssembler,
            OriginOfValueAddedInFinalDemandAssembler originOfValueAddedInFinalDemandAssembler,
            PagedResourcesAssembler<OriginOfValueAddedInGrossImports> originOfValueAddedInGrossImportsPagedResourcesAssembler,
            OriginOfValueAddedInGrossImportsAssembler originOfValueAddedInGrossImportsAssembler,
            PagedResourcesAssembler<GrossExportByOriginOfValueAddedAndFinalDestinations>
                    grossExportByOriginOfValueAddedAndFinalDestinationsPagedResourcesAssembler,
            GrossExportsByOriginOfValueAddedAndFinalDestinationAssembler
                    grossExportsByOriginOfValueAddedAndFinalDestinationAssembler
    ) {
        this.valueAndTradeFlowService = valueAndTradeFlowService;
        this.countryCodePagedResourcesAssembler = countryCodePagedResourcesAssembler;
        this.originOfValueAddedInFinalDemandPagedResourcesAssembler = originOfValueAddedInFinalDemandPagedResourcesAssembler;
        this.tradeLocationCodeModelAssembler = tradeLocationCodeModelAssembler;
        this.originOfValueAddedInFinalDemandAssembler = originOfValueAddedInFinalDemandAssembler;
        this.originOfValueAddedInGrossImportsPagedResourcesAssembler = originOfValueAddedInGrossImportsPagedResourcesAssembler;
        this.originOfValueAddedInGrossImportsAssembler = originOfValueAddedInGrossImportsAssembler;
        this.grossExportByOriginOfValueAddedAndFinalDestinationsPagedResourcesAssembler =
                grossExportByOriginOfValueAddedAndFinalDestinationsPagedResourcesAssembler;
        this.grossExportsByOriginOfValueAddedAndFinalDestinationAssembler =
                grossExportsByOriginOfValueAddedAndFinalDestinationAssembler;
    }

    @Operation(summary = "List all codes available in tiva knowledge graph depends on selected type of code")
    @GetMapping(value = "/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ValueAndTradeFlowModel>> getCodes(
            @Parameter(description = "Type of trade location", example = "Country,InetrnationalOrganization,IndustryCode")
            @RequestParam String type,
            Pageable pageable
    ) {

        Page<ValueAndTradeFlowCode> countryCodePage = valueAndTradeFlowService.
                getValueAndTradeFlowCodeList(sparqlEndPoint, type, pageable);

        PagedModel<ValueAndTradeFlowModel> pagedModel = PageUtils.toPagedModel(
                countryCodePage,
                ValueAndTradeFlowModel.class,
                countryCodePagedResourcesAssembler,
                tradeLocationCodeModelAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "List of country codes, industry codes in final demand, values and dates based " +
            "on selected value added origin country and industry codes. Number of results is limited up to 5000000 n-tuples.")
    @GetMapping(value = "/vao/finaldemand", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<OriginOfValueAddedInFinalDemandModel>> getOriginOfValueAddedInFinalDemand(
            @Parameter(description = "Trade location code for value added origin", example = "DEU, G20")
            @RequestParam String location,
            @Parameter(description = "Industry code for value added origin", example = "D62T63, D20")
            @RequestParam String industry,
            Pageable pageable
    ) {

        Page<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandPage = valueAndTradeFlowService.
                getOriginOfValueAddedList(sparqlEndPoint,
                        location,
                        industry,
                        getValueAddedOriginInFinalDemandQuery(location, industry),
                        pageable);

        PagedModel<OriginOfValueAddedInFinalDemandModel> pagedModel = PageUtils.toPagedModel(
                originOfValueAddedInFinalDemandPage,
                OriginOfValueAddedInFinalDemandModel.class,
                originOfValueAddedInFinalDemandPagedResourcesAssembler,
                originOfValueAddedInFinalDemandAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "List of country codes, industry codes in gross exports, values and dates based " +
            "on selected value added origin country and industry codes. Number of results is limited up to 5000000 n-tuples.")
    @GetMapping(value = "/vao/exports", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<OriginOfValueAddedInFinalDemandModel>> getOriginOfValueAddedInGrossExports(
            @Parameter(description = "Trade location code for value added origin", example = "DEU,")
            @RequestParam String location,
            @Parameter(description = "Industry code for value added origin", example = "D62T63,D20")
            @RequestParam String industry,
            Pageable pageable
    ) {

        Page<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandPage = valueAndTradeFlowService.
                getOriginOfValueAddedList(sparqlEndPoint,
                        location,
                        industry,
                        getValueAddedOriginInGrossExportsQuery(location, industry),
                        pageable);

        PagedModel<OriginOfValueAddedInFinalDemandModel> pagedModel = PageUtils.toPagedModel(
                originOfValueAddedInFinalDemandPage,
                OriginOfValueAddedInFinalDemandModel.class,
                originOfValueAddedInFinalDemandPagedResourcesAssembler,
                originOfValueAddedInFinalDemandAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "List of country codes, industry codes within gross exports, country code wuthin imports, " +
            "value and year in origin of value added in gross imports. Number of results is limited up to 5000000 n-tuples")
    @GetMapping(value = "/vao/imports", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<OriginOfValueAddedInGrossImportsModel>> getOriginOfValueAddedinGrossImports(
            @Parameter(description = "Trade location code for value added origin", example = "DEU")
            @RequestParam String location,
            Pageable pageable
    ) {

        Page<OriginOfValueAddedInGrossImports> originOfValueAddedInGrossImportsPage =
                valueAndTradeFlowService.getOriginOfValueAddedInGrossImports(
                        sparqlEndPoint,
                        location,
                        getOriginOfValueAddedInGrossImportQuery(location),
                        pageable);

        PagedModel<OriginOfValueAddedInGrossImportsModel> pagedModel = PageUtils.toPagedModel(
                originOfValueAddedInGrossImportsPage,
                OriginOfValueAddedInGrossImportsModel.class,
                originOfValueAddedInGrossImportsPagedResourcesAssembler,
                originOfValueAddedInGrossImportsAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "List of country codes, industry codes within gross exports, country code wuthin final demand, " +
            "value and year in  gross exports of vao and final destination. Number of results is limited up to 1000000 n-tuples")
    @GetMapping(value = "/vao/finaldestination", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<GrossExportByOriginOfValueAddedAndFinalDestinationModel>>
    getGrossExportsByOriginOfValueAddedAndFinalDestination(
            @Parameter(description = "Trade location code for value added origin", example = "DEU")
            @RequestParam String location,
            Pageable pageable
    ) {

        Page<GrossExportByOriginOfValueAddedAndFinalDestinations> grossExportsByOriginOfValueAddedAndFinalDestinationPage =
                valueAndTradeFlowService.getGrossExportsByOriginOfValueAddedAndFinalDestination(
                        sparqlEndPoint,
                        location,
                        getGrossExportsByOriginOfValueAddedAndFinalDestinationQuey(location),
                        pageable);

        PagedModel<GrossExportByOriginOfValueAddedAndFinalDestinationModel> pagedModel = PageUtils.toPagedModel(
                grossExportsByOriginOfValueAddedAndFinalDestinationPage,
                GrossExportByOriginOfValueAddedAndFinalDestinationModel.class,
                grossExportByOriginOfValueAddedAndFinalDestinationsPagedResourcesAssembler,
                grossExportsByOriginOfValueAddedAndFinalDestinationAssembler
        );

        return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "Get data from eurostat dataset", hidden = true)
    @GetMapping(value = "/eurostat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getEurostat(
            @Parameter(description = "Country of the import or export of the product", example = "AT")
            @RequestParam(required = false) String reporter,
            @Parameter(description = "Last known country of destination for intra- and extra-EU exports, the country of origin for extra-EU imports and the country of consignment for intra-EU imports", example = "EU_EXTRA")
            @RequestParam(required = false) String partner,
            @Parameter(description = "Import or Export", example = "1")
            @RequestParam(required = false) String flow,
            @Parameter(description = "Product code from harmonized system (HS)", example = "854149")
            @RequestParam(required = false) String product,
            @Parameter(description = "Years from the system", example = "1995")
            @RequestParam(required = false) String year,
            @Parameter(description = "Value of the chosen product", example = "1999")
            @RequestParam(required = false) String value
    ) {
        val fields = new LinkedHashSet<String>();

        if (reporter != null) {
            fields.add("reporter");
        }
        if (partner != null) {
            fields.add("partner");
        }
        if (flow != null) {
            fields.add("flow");
        }
        if (product != null) {
            fields.add("product");
        }
        if (year != null) {
            fields.add("yearTrimmed");
        }
        if (value != null) {
            fields.add("value");
        }
        val result = getListEurostat(fields);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get sum value by reporter and year")
    @GetMapping(value = "/euroRepYearSum", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<ChartObj>>> getEuroRepYearSum(
            @Parameter(description = "Reporter", example = "AT")
            @RequestParam String reporter,
            @Parameter(description = "Year", example = "1996")
            @RequestParam String year
    ) {

        val result = getRepYearSum(reporter, year);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get sum value by reporter and year range")
    @GetMapping(value = "/euroYearRange", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<ChartObj>>> getAggregatedYearRange(
            @Parameter(description = "List of reporters", example = "AT")
            @RequestParam List<String> reporters,
            @Parameter(description = "Year start", example = "1996-01-01")
            @RequestParam String start,
            @Parameter(description = "Year end", example = "1998-12-31")
            @RequestParam String end,
            @Parameter(description = "List of partners", example = "EU_EXTRA")
            @RequestParam List<String> partners,
            @Parameter(description = "Flow", example = "1")
            @RequestParam(required = false) String flow,
            @Parameter(description = "Products", example = "854149")
            @RequestParam List<String> products

    ) {
        val result = getYearRange(reporters, start, end, partners, flow, products);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get sum of values between reporter and partner groupped by product")
    @GetMapping(value = "/euroRepParProd", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getEuroRep(
            @Parameter(description = "List of reporters", example = "AT")
            @RequestParam List<String> reporters,
            @Parameter(description = "Year start", example = "1996-01-01")
            @RequestParam String start,
            @Parameter(description = "Year end", example = "1998-12-31")
            @RequestParam String end,
            @Parameter(description = "List of partners", example = "EU_EXTRA")
            @RequestParam List<String> partners,
            @Parameter(description = "Flow", example = "1")
            @RequestParam(required = false) String flow,
            @Parameter(description = "Products", example = "854149")
            @RequestParam List<String> products
    ) {
        val result = getReporterPartnerProducts(reporters, start, end, partners, flow, products);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public static List<String> getListEurostat(Set<String> fields) {
        val objects = new ArrayList<String>();

        String query = getQueryByFields(fields);

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(query);

        try (QueryExecution q = QueryExecutionHTTP.service("http://sc3onto01.develop.service.tib.eu:7200/repositories/default")
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();

            while (results.hasNext()) {
                QuerySolution solution = results.next();
                for (String field : fields) {
                    String key = "";
                    if (solution.contains(field)) {
                        if (field.equals("flow") || field.equals("value") || field.equals("yearTrimmed") || field.equals("product")) {
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

    private static String getQueryByFields(Set<String> fields) {
        String prefixes = Queries.EUROSTAT_PREFIXES;

        StringBuilder select = new StringBuilder("SELECT DISTINCT");
        StringBuilder where = new StringBuilder(Queries.EUROSTAT_WHERE_CLAUSE);

        for (String field : fields) {
            switch (field) {
                case "reporter":
                    select.append(" ?reporter");
                    where.append(" ?data <https://schema.coypu.org/global#hasReporter> ?reporter .");
                    break;
                case "partner":
                    select.append(" ?partner");
                    where.append(" ?data <https://schema.coypu.org/global#hasPartner> ?partner .");
                    break;
                case "product":
                    select.append(" ?product");
                    where.append(" ?data <http://www.w3id.org/ecsel-dr-OOSMP#contains_product> ?product .");
                    break;
                case "flow":
                    select.append(" ?flow");
                    where.append(" ?data <https://schema.coypu.org/global#hasDataFlow> ?flow .");
                    break;
                case "value":
                    select.append(" ?value");
                    where.append("?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .")
                            .append(" ?data_value <http://www.w3.org/2006/vcard/ns#value> ?value .");
                    break;
                case "yearTrimmed":
                    select.append(" (SUBSTR(?year, 0, spif:indexOf(?year, '-') + 1) AS ?yearTrimmed)");
                    where.append("?data <http://www.w3.org/2006/vcard/ns#hasValue> ?data_value .")
                            .append(" ?data_value <http://publications.europa.eu/ontology/cdm#year>  ?year .");
                    break;
            }
        }

        return prefixes + select + where + "\n}";
    }

    public static Map<String, List<ChartObj>> getRepYearSum(String reporter, String year) {
        val objects = new HashMap<String, List<ChartObj>>();
        val obj = new ArrayList<ChartObj>();

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(Queries.EUROSTAT_REP_YEAR_SUM);
        graphQuery.setLiteral("repArg", reporter);
        graphQuery.setLiteral("yearArg", year);
        try (QueryExecution q = QueryExecutionHTTP.service("http://sc3onto01.develop.service.tib.eu:7200/repositories/default")
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get("yearTrimmed").asLiteral().getValue().toString();
                val sumVal = solution.get("sumVal").asLiteral().getValue().toString();
                obj.add(new ChartObj(yearTrimmed, sumVal));
                objects.put("data", obj);
            }
        }
        return objects;
    }

    private static List<String> assignFullAtoldPrefix(List<String> data) {
        return data.stream()
                .map(item -> "<http://publications.europa.eu/resource/authority/country/" + item + ">")
                .toList();
    }

    private static List<String> assignCoypuHsPrefix(List<String> data) {
        return data.stream()
                .map(item -> "<https://data.coypu.org/classification/hs_2012/" + item + ">")
                .toList();
    }

    private static String generateValuesClause(String field, List<String> inputs) {
        StringBuilder values = new StringBuilder("VALUES ?").append(field).append(" {");
        for (String input : inputs) {
            values.append(input).append(System.lineSeparator());
        }
        values.append(" }").append(System.lineSeparator());
        return values.toString();
    }

    public static Map<String, String> getReporterPartnerProducts(List<String> reporters, String startDate, String endDate, List<String> partners, String flow, List<String> products) {
        val objects = new HashMap<String, String>();

        val fullReportersIRIs = assignFullAtoldPrefix(reporters);
        val fullPartnersIRIs = assignFullAtoldPrefix(partners);
        val fullProductsIRIs = assignCoypuHsPrefix(products);
        String prefixes = Queries.EUROSTAT_PREFIXES;
        StringBuilder select = new StringBuilder("""
                        SELECT DISTINCT ?reporter ?partner ?product
                        (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?sumVal)
                """);

        String reporterValues = generateValuesClause("reporter", fullReportersIRIs);
        String partnerValues = generateValuesClause("partner", fullPartnersIRIs);
        String productValues = generateValuesClause("product", fullProductsIRIs);

        StringBuilder where = new StringBuilder("WHERE {\n")
                .append(reporterValues)
                .append(partnerValues)
                .append(productValues)
                .append(Queries.EUROSTAT_FULL_WHERE_CLAUSE);

        StringBuilder filters = new StringBuilder("FILTER(STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) >= ?startDate^^xsd:date &&\n")
                .append("       STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) <= ?endDate^^xsd:date) .");

        if (flow != null) {
            filters.append("\nFILTER(CONTAINS(STR(?flow), ?flowArg)) .");
        }

        String query = prefixes + select + where + filters + "\n} GROUP BY ?product ?reporter ?partner";
        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(query);
        graphQuery.setLiteral("startDate", startDate);
        graphQuery.setLiteral("endDate", endDate);
        if (flow != null) {
            graphQuery.setLiteral("flowArg", flow);
        }

        try (QueryExecution q = QueryExecutionHTTP.service("http://sc3onto01.develop.service.tib.eu:7200/repositories/default")
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val productVal = solution.get("product").asResource().toString();
                val reporterVal = solution.get("reporter").asResource().getLocalName();
                val partnerVal = solution.get("partner").asResource().getLocalName();
                val sumVal = solution.get("sumVal").asLiteral().getValue().toString();

                String key = productVal.substring(productVal.lastIndexOf('/') + 1) + "_" + reporterVal + "_" + partnerVal;
                objects.put(key, sumVal);
            }
        }

        return objects;
    }

    public static Map<String, List<ChartObj>> getYearRange(
            List<String> reporters, String startDate, String endDate, List<String> partners, String flow, List<String> products) {

        val objects = new HashMap<String, List<ChartObj>>();

        val fullReportersIRIs = assignFullAtoldPrefix(reporters);
        val fullPartnersIRIs = assignFullAtoldPrefix(partners);
        val fullProductsIRIs = assignCoypuHsPrefix(products);

        String prefixes = Queries.EUROSTAT_PREFIXES;
        StringBuilder select = new StringBuilder("""
                        SELECT DISTINCT ?reporter (STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) AS ?yearTrimmed)
                        (SUM(xsd:decimal(SUBSTR(?value, 0, spif:lastIndexOf(?value, "^")))) AS ?sumVal)
                        ?partner
                """);

        String reporterValues = generateValuesClause("reporter", fullReportersIRIs);
        String partnerValues = generateValuesClause("partner", fullPartnersIRIs);
        String productValues = generateValuesClause("product", fullProductsIRIs);

        StringBuilder where = new StringBuilder("WHERE {\n")
                .append(reporterValues)
                .append(partnerValues)
                .append(productValues)
                .append(Queries.EUROSTAT_FULL_WHERE_CLAUSE);

        StringBuilder filters = new StringBuilder("FILTER(STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) >= ?startDate^^xsd:date &&\n")
                .append("       STRDT(CONCAT(SUBSTR(?year, 1, 7), '-01'), xsd:date) <= ?endDate^^xsd:date) .");

        if (flow != null) {
            filters.append("\nFILTER(CONTAINS(STR(?flow), ?flowArg)) .");
        }

        String query = prefixes + select + where + filters + "\n} GROUP BY ?reporter ?year ?partner";

        ParameterizedSparqlString graphQuery = new ParameterizedSparqlString();
        graphQuery.setCommandText(query);
        graphQuery.setLiteral("startDate", startDate);
        graphQuery.setLiteral("endDate", endDate);

        if (flow != null) {
            graphQuery.setLiteral("flowArg", flow);
        }

        try (QueryExecution q = QueryExecutionHTTP.service("http://sc3onto01.develop.service.tib.eu:7200/repositories/default")
                .query(graphQuery.asQuery())
                .build()) {

            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                val yearTrimmed = solution.get("yearTrimmed").asLiteral().getValue().toString();
                val sumVal = solution.get("sumVal").asLiteral().getValue().toString();
                val reporterVal = solution.get("reporter").asResource().getLocalName();
                val partnerVal = solution.get("partner").asResource().getLocalName();

                String key = reporterVal + "_" + partnerVal;

                val obj = objects.getOrDefault(key, new ArrayList<>());
                obj.add(new ChartObj(yearTrimmed, sumVal));
                objects.put(key, obj);
            }
        }

        return objects;
    }

    public static String getValueAddedOriginInFinalDemandQuery(String location, String industryCode) {

        String queryString = "";
        String localtionUri = "";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
                location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
                location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
                location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
                location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
                location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

            localtionUri = "<https://data.coypu.org/organization/" + location + ">";

            log.info("selected location code in final demand: " + localtionUri);

        } else {

            localtionUri = "<https://data.coypu.org/country/" + location + ">";

            log.info("selected location code in final demand: " + localtionUri);

        }

        queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT DISTINCT ?fdTradeLocation ?fdIndustryCode ?vao_fd_value  ?vao_fd_year " +
                "WHERE { " +
                "?vao_fd rdf:type <https://schema.coypu.org/vtf#FdVaBsci> . " +
                "?vao_fd <https://schema.coypu.org/global#hasValue> ?vao_fd_value . " +
                "?vao_fd <https://schema.coypu.org/global#hasYear> ?vao_fd_year . " +
                "?vao_fd <https://schema.coypu.org/vtf#hasValueAddedOrigin> ?vao . " +
                "?vao rdf:type <https://schema.coypu.org/vtf#Vao> . " +
                "?vao <https://schema.coypu.org/vtf#hasIndustryCode> <https://data.coypu.org/classification/tiva-21/" + industryCode + "> . " +
                "?vao  <https://schema.coypu.org/vtf#hasTradeLocation> " + localtionUri + " . " +
                "?vao_fd <https://schema.coypu.org/vtf#hasFinalDemand> ?fd . " +
                "?fd rdf:type <https://schema.coypu.org/vtf#Fd> . " +
                "?fd <https://schema.coypu.org/vtf#hasIndustryCode> ?fdIndustryCode . " +
                "?fd <https://schema.coypu.org/vtf#hasTradeLocation> ?fdTradeLocation . " +
                "} LIMIT 5000000 ";

        return queryString;
    }

    public static String getValueAddedOriginInGrossExportsQuery(String location, String industryCode) {

        String queryString = "";
        String locationIri = "";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
                location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
                location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
                location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
                location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
                location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

            locationIri = "<https://data.coypu.org/organization/" + location + ">";

            log.info("selected location code in exports: " + locationIri);

        } else {

            locationIri = "<https://data.coypu.org/country/" + location + ">";

            log.info("selected location code in exports: " + locationIri);

        }

        queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT DISTINCT ?fdTradeLocation ?fdIndustryCode ?vao_fd_value  ?vao_fd_year " +
                "WHERE { " +
                "?vao_ex rdf:type <https://schema.coypu.org/vtf#ExgrBsci> . " +
                "?vao_ex <https://schema.coypu.org/global#hasValue> ?vao_fd_value . " +
                "?vao_ex <https://schema.coypu.org/global#hasYear> ?vao_fd_year . " +
                "?vao_ex <https://schema.coypu.org/vtf#hasValueAddedOrigin> ?vao . " +
                "?vao rdf:type <https://schema.coypu.org/vtf#Vao> . " +
                "?vao <https://schema.coypu.org/vtf#hasIndustryCode> <https://data.coypu.org/classification/tiva-21/" + industryCode + "> . " +
                "?vao  <https://schema.coypu.org/vtf#hasTradeLocation> " + locationIri + " . " +
                "?vao_ex <https://schema.coypu.org/vtf#hasExport> ?ex . " +
                "?ex rdf:type <https://schema.coypu.org/vtf#Export> . " +
                "?ex <https://schema.coypu.org/vtf#hasIndustryCode> ?fdIndustryCode . " +
                "?ex <https://schema.coypu.org/vtf#hasTradeLocation> ?fdTradeLocation . " +
                "} LIMIT 5000000 ";

        return queryString;
    }

    public static String getOriginOfValueAddedInGrossImportQuery(String location) {

        String queryString = "";
        String locationIri = "";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
                location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
                location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
                location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
                location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
                location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

            locationIri = "<https://data.coypu.org/organization/" + location + ">";

            log.info("selected location code in import: " + locationIri);

        } else {

            locationIri = "<https://data.coypu.org/country/" + location + ">";

            log.info("selected location code in import " + locationIri);

        }

        queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT DISTINCT ?exTradeLocation ?exIndustryCode ?importTradeLocation ?vao_import_value ?vao_import_year " +
                "WHERE { " +
                "?vao_import rdf:type <https://schema.coypu.org/vtf#ImgrBsci> . " +
                "?vao_import <https://schema.coypu.org/global#hasValue> ?vao_import_value . " +
                "?vao_import <https://schema.coypu.org/global#hasYear> ?vao_import_year . " +
                "?vao_import <https://schema.coypu.org/vtf#hasValueAddedOrigin> ?vao . " +
                "?vao rdf:type <https://schema.coypu.org/vtf#Vao> . " +
                "?vao  <https://schema.coypu.org/vtf#hasTradeLocation> " + locationIri + " . " +
                "?vao_import <https://schema.coypu.org/vtf#hasExport> ?ex . " +
                "?ex rdf:type <https://schema.coypu.org/vtf#Export> . " +
                "?ex <https://schema.coypu.org/vtf#hasIndustryCode> ?exIndustryCode . " +
                "?ex <https://schema.coypu.org/vtf#hasTradeLocation> ?exTradeLocation . " +
                "?vao_import <https://schema.coypu.org/vtf#hasImport> ?import . " +
                "?import rdf:type <https://schema.coypu.org/vtf#Import> . " +
                "?import <https://schema.coypu.org/vtf#hasTradeLocation> ?importTradeLocation . " +
                "} LIMIT 5000000 ";

        return queryString;

    }

    public static String getGrossExportsByOriginOfValueAddedAndFinalDestinationQuey(String location) {

        String queryString = "";
        String locationIri = "";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
                location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
                location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
                location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
                location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
                location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

            locationIri = "<https://data.coypu.org/organization/" + location + ">";

            log.info("selected location code in import: " + locationIri);

        } else {

            locationIri = "<https://data.coypu.org/country/" + location + ">";

            log.info("selected location code in import " + locationIri);

        }

        queryString =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "SELECT DISTINCT ?exTradeLocation ?exIndustryCode ?fdTradeLocation ?fd_exgr_va_year ?fd_exgr_va_value " +
                        "WHERE { " +
                        "?fd_exgr_va rdf:type <https://schema.coypu.org/vtf#FdExgrVa> . " +
                        "?fd_exgr_va <https://schema.coypu.org/global#hasValue> ?fd_exgr_va_value . " +
                        "?fd_exgr_va <https://schema.coypu.org/global#hasYear> ?fd_exgr_va_year . " +
                        "?fd_exgr_va <https://schema.coypu.org/vtf#hasValueAddedOrigin> ?vao . " +
                        "?vao rdf:type <https://schema.coypu.org/vtf#Vao> . " +
                        "?vao <https://schema.coypu.org/vtf#hasTradeLocation>" + locationIri + " . " +
                        "?fd_exgr_va <https://schema.coypu.org/vtf#hasExport> ?fd_exgr_va_export . " +
                        "?fd_exgr_va_export rdf:type <https://schema.coypu.org/vtf#Export> . " +
                        "?fd_exgr_va_export <https://schema.coypu.org/vtf#hasIndustryCode> ?exIndustryCode . " +
                        "?fd_exgr_va_export <https://schema.coypu.org/vtf#hasTradeLocation> ?exTradeLocation . " +
                        "?fd_exgr_va <https://schema.coypu.org/vtf#hasFinalDemand> ?fd_exgr_va_fd . " +
                        "?fd_exgr_va_fd rdf:type <https://schema.coypu.org/vtf#Fd> . " +
                        "?fd_exgr_va_fd <https://schema.coypu.org/vtf#hasTradeLocation> ?fdTradeLocation . " +
                        "} LIMIT 1000000 ";

        return queryString;

    }


}