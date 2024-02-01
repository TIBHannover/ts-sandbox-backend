package eu.tib.tiva.controller;

import eu.tib.tiva.controller.assembler.OriginOfValueAddedInGrossImportsAssembler;
import eu.tib.tiva.controller.dto.OriginOfValueAddedInFinalDemandModel;
import eu.tib.tiva.controller.dto.OriginOfValueAddedInGrossImportsModel;
import eu.tib.tiva.controller.dto.ValueAndTradeFlowModel;
import eu.tib.tiva.controller.assembler.OriginOfValueAddedInFinalDemandAssembler;
import eu.tib.tiva.controller.assembler.TradeLocationCodeModelAssembler;
import eu.tib.tiva.model.*;
import eu.tib.tiva.service.ValueAndTradeFlowService;
import eu.tib.tiva.utils.HttpUtils;
import eu.tib.tiva.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final String sparqlEndPoint = "https://tiva.coypu.org/tiva";

    @Autowired
    public ValueAndTradeFlowController(
            ValueAndTradeFlowService valueAndTradeFlowService,
            PagedResourcesAssembler<ValueAndTradeFlowCode> countryCodePagedResourcesAssembler,
            PagedResourcesAssembler<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandPagedResourcesAssembler,
            TradeLocationCodeModelAssembler tradeLocationCodeModelAssembler,
            OriginOfValueAddedInFinalDemandAssembler originOfValueAddedInFinalDemandAssembler,
            PagedResourcesAssembler<OriginOfValueAddedInGrossImports> originOfValueAddedInGrossImportsPagedResourcesAssembler,
            OriginOfValueAddedInGrossImportsAssembler originOfValueAddedInGrossImportsAssembler){
        this.valueAndTradeFlowService = valueAndTradeFlowService;
        this.countryCodePagedResourcesAssembler=countryCodePagedResourcesAssembler;
        this.originOfValueAddedInFinalDemandPagedResourcesAssembler=originOfValueAddedInFinalDemandPagedResourcesAssembler;
        this.tradeLocationCodeModelAssembler = tradeLocationCodeModelAssembler;
        this.originOfValueAddedInFinalDemandAssembler=originOfValueAddedInFinalDemandAssembler;
        this.originOfValueAddedInGrossImportsPagedResourcesAssembler=originOfValueAddedInGrossImportsPagedResourcesAssembler;
        this.originOfValueAddedInGrossImportsAssembler=originOfValueAddedInGrossImportsAssembler;
    }

    @Operation(summary = "List all codes available in tiva knowledge graph depends on selected type of code")
    @GetMapping(value = "/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ValueAndTradeFlowModel>> getCodes(
            @Parameter(description = "Type of trade location", example = "Country,InetrnationalOrganization,IndustryCode")
            @RequestParam String type,
            Pageable pageable
    ){

        Page<ValueAndTradeFlowCode> countryCodePage = valueAndTradeFlowService.
                getValueAndTradeFlowCodeList(sparqlEndPoint,type,pageable);

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
    ){

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
    ){

        Page<OriginOfValueAddedInFinalDemand> originOfValueAddedInFinalDemandPage = valueAndTradeFlowService.
                getOriginOfValueAddedList(sparqlEndPoint,
                        location,
                        industry,
                        getValueAddedOriginInGrossExportsQuery(location, industry) ,
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
    @GetMapping(value="/vao/imports", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<OriginOfValueAddedInGrossImportsModel>> getOriginOfValueAddedinGrossImports(
            @Parameter(description = "Trade location code for value added origin", example = "DEU")
            @RequestParam String location,
            Pageable pageable
    ){

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


    public static String getValueAddedOriginInFinalDemandQuery(String location, String industryCode) {

        String queryString =  "";
        String localtionUri = "";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
            location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
            location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
            location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
            location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
            location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

        localtionUri="<https://data.coypu.org/organization/"+location+">";

        log.info("selected location code in final demand: " + localtionUri);

        } else {

        localtionUri ="<https://data.coypu.org/country/"+location+">";

        log.info("selected location code in final demand: " + localtionUri);

        }

        queryString =  "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
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
        String locationIri="";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
                location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
                location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
                location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
                location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
                location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

            locationIri="<https://data.coypu.org/organization/"+location+">";

            log.info("selected location code in exports: " + locationIri);

        } else {

            locationIri ="<https://data.coypu.org/country/"+location+">";

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
        String locationIri="";

        if (location.equals("APEC") || location.equals("ECD") || location.equals("EU13") ||
                location.equals("EASIA") || location.equals("G20") || location.equals("EU28") ||
                location.equals("EU15") || location.equals("ZASI") || location.equals("EA19") ||
                location.equals("ZSCA") || location.equals("WLD") || location.equals("DXD") ||
                location.equals("ZEUR") || location.equals("ZOTH") || location.equals("ZNAM") ||
                location.equals("NONOECD") || location.equals("ASEAN") || location.equals("EU27_2020")
        ) {

            locationIri="<https://data.coypu.org/organization/"+location+">";

            log.info("selected location code in import: " + locationIri);

        } else {

            locationIri ="<https://data.coypu.org/country/"+location+">";

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
                "?vao  <https://schema.coypu.org/vtf#hasTradeLocation> "+locationIri+ " . " +
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

}