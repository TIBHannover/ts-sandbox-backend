package eu.tib.tiva.controller;

import eu.tib.tiva.controller.assembler.CountryCodeModelAssembler;
import eu.tib.tiva.controller.dto.CountryCodeModel;
import eu.tib.tiva.model.CountryCode;
import eu.tib.tiva.model.CountryCodesModel;
import eu.tib.tiva.service.CountryCodeService;
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
public class CountryCodeController {

    private final CountryCodeService countryCodeService;
    private final PagedResourcesAssembler<CountryCode> countryCodePagedResourcesAssembler;

    private final CountryCodeModelAssembler countryCodeModelAssembler;

    @Autowired
    public CountryCodeController(
            CountryCodeService countryCodeService,
            PagedResourcesAssembler<CountryCode> countryCodePagedResourcesAssembler,
            CountryCodeModelAssembler countryCodeModelAssembler)
    {

        this.countryCodeService=countryCodeService;
        this.countryCodePagedResourcesAssembler=countryCodePagedResourcesAssembler;
        this.countryCodeModelAssembler=countryCodeModelAssembler;
    }
    @Operation(summary = "List all country codes available in tiva knowledge graph")
    @GetMapping(value = "/countrycodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<CountryCodeModel>> getAllCountryCodes(
            @Parameter(description = "Skynet TiVA SPARQL endpoint")
            @RequestParam String skyNetTivaSparqlEndpoint,
            @Parameter(description = "A user name for acceessing Skynet server (SPARLQ endpoint) ", example = "skynet-user")
            @RequestParam String userName,
            @Parameter(description = "Password", example = "abc123")
            @RequestParam String password,
            Pageable pageable
    ){

        Page<CountryCode> countryCodePage = countryCodeService.queryCountryCodes(skyNetTivaSparqlEndpoint,userName,password,pageable);

        PagedModel<CountryCodeModel> pagedModel = PageUtils.toPagedModel(
                countryCodePage,
                CountryCodeModel.class,
                countryCodePagedResourcesAssembler,
                countryCodeModelAssembler
        );

        return HttpUtils.ok(pagedModel);

    }


}
