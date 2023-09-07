package eu.tib.ts.controller;

import eu.tib.ts.controller.assember.KeyValueModelAssembler;
import eu.tib.ts.controller.dto.KeyValueResultDto;
import eu.tib.ts.model.ontology.CharacteristicsType;
import eu.tib.ts.model.ontology.KeyValueModel;
import eu.tib.ts.service.MostCommonlyUsedService;
import eu.tib.ts.utils.HttpUtils;
import eu.tib.ts.utils.PageUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ontology/mostCommonlyUsed")
public class MostCommonlyUsedController {
    private final MostCommonlyUsedService mostCommonlyUsedService;
    private final PagedResourcesAssembler<KeyValueResultDto> pagedResourcesAssembler;
    private final KeyValueModelAssembler modelAssembler;

    @Autowired
    public MostCommonlyUsedController(MostCommonlyUsedService mostCommonlyUsedService,
                                      PagedResourcesAssembler<KeyValueResultDto> pagedResourcesAssembler,
                                      KeyValueModelAssembler modelAssembler) {
        this.mostCommonlyUsedService = mostCommonlyUsedService;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.modelAssembler = modelAssembler;
    }

    @Operation(summary = "Most commonly used Properties | Classes | Imports | Namespaces")
    @GetMapping(value = "/{characteristics}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<KeyValueModel>> getMostCommonlyUsed(
        @Parameter(description = "Characteristics to be compared by", example = "PROPERTY")
        @PathVariable("characteristics") CharacteristicsType characteristicsType,
        @Parameter(description = "A set of Ontology IDs managed in the TS", example = "dicl,dicob")
        @RequestParam(required = false) Optional<List<String>> ids,
        @Parameter(description = "Collection to filter set of ontologies", example = "NFDI4ING")
        @RequestParam(required = false) Optional<String> collection,
        Pageable pageable
    ) {
        Page<KeyValueResultDto> page =
            mostCommonlyUsedService.getMostCommonlyUsedCharacteristics(ids, characteristicsType, collection, pageable);

        PagedModel<KeyValueModel> pagedModel =
            PageUtils.toPagedModel(page, KeyValueModel.class, pagedResourcesAssembler, modelAssembler);

        return HttpUtils.ok(pagedModel);
    }
}
