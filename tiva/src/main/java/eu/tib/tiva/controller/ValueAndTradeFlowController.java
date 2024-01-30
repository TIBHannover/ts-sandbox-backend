package eu.tib.tiva.controller;

import eu.tib.tiva.controller.assembler.TradeLocationCodeModelAssembler;
import eu.tib.tiva.controller.dto.ValueAndTradeFlowModel;
import eu.tib.tiva.model.ValueAndTradeFlowCode;
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

    private final String sparqlEndPoint = "https://tiva.coypu.org/tiva";

    @Autowired
    public ValueAndTradeFlowController(
            ValueAndTradeFlowService valueAndTradeFlowService,
            PagedResourcesAssembler<ValueAndTradeFlowCode> countryCodePagedResourcesAssembler,
            TradeLocationCodeModelAssembler tradeLocationCodeModelAssembler){
        this.valueAndTradeFlowService = valueAndTradeFlowService;
        this.countryCodePagedResourcesAssembler=countryCodePagedResourcesAssembler;
        this.tradeLocationCodeModelAssembler = tradeLocationCodeModelAssembler;
    }

    @Operation(summary = "List all trade location codes available in tiva knowledge graph dependes on selected type of trade location")
    @GetMapping(value = "/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ValueAndTradeFlowModel>> getCodes(
            @Parameter(description = "Type of trade location", example = "Country, InetrnationalOrganization, IndustryCode")
            @RequestParam String type,
            Pageable pageable
    ){

        Page<ValueAndTradeFlowCode> countryCodePage = valueAndTradeFlowService.getValueAndTradeFlowCodeList(sparqlEndPoint,type,pageable);

        PagedModel<ValueAndTradeFlowModel> pagedModel = PageUtils.toPagedModel(
                countryCodePage,
                ValueAndTradeFlowModel.class,
                countryCodePagedResourcesAssembler,
                tradeLocationCodeModelAssembler
        );

    return HttpUtils.ok(pagedModel);

    }

    @Operation(summary = "List of country codes, industry codes, values and dates based " +
            "on selected value added origin country, industry codes, and selected type of vale and trade flows.")
    @GetMapping(value = "/vao", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<ValueAndTradeFlowModel>> getOriginOfValueAddedInFinalDemandOrGrossExports(
            @Parameter(description = "Type of value and trade flows (final demand, gross exports)",
                    example = "final demand or gross exports")
            @RequestParam String type,
            @Parameter(description = "Trade location code for value added origin", example = "DEU, EU19")
            @RequestParam String location,
            @Parameter(description = "Industry code for value added origin", example = "D62T63, D20")
            @RequestParam String industry,
            Pageable pageable
    ){

        Page<ValueAndTradeFlowCode> countryCodePage = valueAndTradeFlowService.getValueAndTradeFlowCodeList(sparqlEndPoint,location,pageable);

        PagedModel<ValueAndTradeFlowModel> pagedModel = PageUtils.toPagedModel(
                countryCodePage,
                ValueAndTradeFlowModel.class,
                countryCodePagedResourcesAssembler,
                tradeLocationCodeModelAssembler
        );

        return HttpUtils.ok(pagedModel);

    }
}