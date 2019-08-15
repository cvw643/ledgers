package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.rest.annotation.PostingResource;
import de.adorsys.ledgers.rest.posting.api.PostingApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@PostingResource
@RequestMapping(PostingApi.BASE_PATH)
@RequiredArgsConstructor
public class HealthController implements PostingApi{
    private final Principal principal;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name) {
        return "greeting " + name + " Principal=" + principal.getName();
    }
}
