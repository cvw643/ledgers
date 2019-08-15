package de.adorsys.ledgers.postings.rest.api.controller;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG009 - Mock-PostingData-Upload")
public interface ChartOfAccountRestApi {
    String BASE_PATH = "/posting";

    @PostMapping(path = "/coas")
    @ApiOperation(value = "Creates a new Chart of Account.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> newChartOfAccount(@RequestBody ChartOfAccountBO chartOfAccount);

    @GetMapping(path = "/coas/{id}")
    @ApiOperation(value = "Finds Chart of Account by identifier.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<ChartOfAccountBO> findChartOfAccountsById(@PathVariable("id") String id);

    @GetMapping(path = "/coas", params = {"name"})
    @ApiOperation(value = "Finds Chart of Account by name.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<ChartOfAccountBO> findChartOfAccountsByName(@RequestParam(name = "name") String name);
}
