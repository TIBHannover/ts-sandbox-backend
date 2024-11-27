package eu.tib.tiva.controller;

import eu.tib.tiva.model.eurostat.ChartObj;
import eu.tib.tiva.service.EurostatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
@AllArgsConstructor
@RequestMapping("/api/tiva/eurostat")
public class EurostatController {

    private EurostatService eurostatService;

    @Operation(summary = "Get data from eurostat dataset", hidden = true)
    @GetMapping
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
            fields.add("year");
        }
        if (value != null) {
            fields.add("value");
        }
        val result = eurostatService.getListEurostat(fields);
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

        val result = eurostatService.getRepYearSum(reporter, year);
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
        val result = eurostatService.getYearRange(reporters, start, end, partners, flow, products);
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
        val result = eurostatService.getReporterPartnerProducts(reporters, start, end, partners, flow, products);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Real GDP growth rate, data is based on TEC00115 - https://ec.europa.eu/eurostat/databrowser/view/tec00115/default/table?lang=en")
    @GetMapping(value = "/gdp", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChartObj>> getGDPGrowthRate(
            @Parameter(description = "List of geo entities", example = "AL")
            @RequestParam List<String> geos,
            @Parameter(description = "Unit of measure: Chain linked volumes, percentage change on previous period or Chain linked volumes, percentage change on previous period, per capita")
            @RequestParam String unit
    ) {
        val result = eurostatService.getGDPInformation(geos, unit);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Unique geo countries for real GDP growth rate")
    @GetMapping(value = "/gdp/geos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getGeoCountriesForRealGDP() {
        val result = eurostatService.getGeoCountries();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Annual enterprise statistics for special aggregates of NACE Rev.2 activities (2005-2020), data is based on SBS_NA_SCA_R2 - https://ec.europa.eu/eurostat/databrowser/view/sbs_na_sca_r2__custom_13394272/default/table?lang=en")
    @GetMapping(value = "/annualEnterprise", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChartObj>> getAnnualEnterprise(
            @Parameter(description = "List of geo entities", example = "BE")
            @RequestParam List<String> geos,
            @Parameter(description = "Economical indicator for structural business statistics")
            @RequestParam String indicator
    ) {
        val result = eurostatService.getAnnualEnteprise(geos, indicator);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get geopolitical entities from Annual enterprise dataset")
    @GetMapping(value = "/annualEnterprise/geos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getAnnualEnterpriseGeos() {
        val result = eurostatService.getAnnualEntepriseGeos();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Sold production, exports and imports, data is based on SBS_NA_SCA_R2 - https://ec.europa.eu/eurostat/databrowser/view/ds-056120__custom_13394028/default/table?lang=en")
    @GetMapping(value = "/soldProduction", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ChartObj>> getSoldProduction(
            @Parameter(description = "List of declared entities", example = "001")
            @RequestParam List<String> declaredEntities,
            @Parameter(description = "Economical indicator for structural business statistics")
            @RequestParam String indicator
    ) {
        val result = eurostatService.getSoldProduction(declaredEntities, indicator);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get declared entities from Sold production dataset")
    @GetMapping(value = "/soldProduction/decl", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getSoldProductionDecl() {
        val result = eurostatService.getSoldProductionDecl();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
