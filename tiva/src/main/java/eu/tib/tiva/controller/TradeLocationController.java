package eu.tib.tiva.controller;

import eu.tib.tiva.controller.assembler.CountryCodeModelAssembler;
import eu.tib.tiva.controller.dto.TradeLocationModel;
import eu.tib.tiva.model.TradeLocationCode;
import eu.tib.tiva.service.TradeLocationService;
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
public class TradeLocationController {

    private final TradeLocationService tradeLocationService;
    private final PagedResourcesAssembler<TradeLocationCode> countryCodePagedResourcesAssembler;

    private final CountryCodeModelAssembler countryCodeModelAssembler;

    @Autowired
    public TradeLocationController(
            TradeLocationService tradeLocationService,
            PagedResourcesAssembler<TradeLocationCode> countryCodePagedResourcesAssembler,
            CountryCodeModelAssembler countryCodeModelAssembler)
    {

        this.tradeLocationService = tradeLocationService;
        this.countryCodePagedResourcesAssembler=countryCodePagedResourcesAssembler;
        this.countryCodeModelAssembler=countryCodeModelAssembler;
    }
    @Operation(summary = "List all trade location codes available in tiva knowledge graph dependes on selected type of trade location")
    @GetMapping(value = "/tradelocationcodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<TradeLocationModel>> getAllCountryCodes(
            @Parameter(description = "Type of trade location", example = "Country, InetrnationalOrganization")
            @RequestParam String type,
            Pageable pageable
    ){

        Page<TradeLocationCode> countryCodePage = tradeLocationService.getCountryCodeList("https://tiva.coypu.org/tiva",type,pageable);

        PagedModel<TradeLocationModel> pagedModel = PageUtils.toPagedModel(
                countryCodePage,
                TradeLocationModel.class,
                countryCodePagedResourcesAssembler,
                countryCodeModelAssembler
        );

        return HttpUtils.ok(pagedModel);

    }
}