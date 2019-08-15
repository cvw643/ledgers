package de.adorsys.ledgers.rest.controller;

import de.adorsys.ledgers.postings.rest.api.controller.HealthRestApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(HealthRestApi.BASE_PATH)
@RequiredArgsConstructor
public class HealthController implements HealthRestApi {
    private final Principal principal;

    @Override
    public String greeting(String name) {
        return "greeting " + name + " Principal=" + principal.getName();
    }
}
