package de.adorsys.ledgers.postings.rest.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG009 - Mock-PostingData-Upload")
public interface HealthRestApi {
    String BASE_PATH = "/posting";

    @GetMapping("/greeting")
    @ApiOperation(value = "Greeting.", authorizations = @Authorization(value = "apiKey"))
    String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name);
}
