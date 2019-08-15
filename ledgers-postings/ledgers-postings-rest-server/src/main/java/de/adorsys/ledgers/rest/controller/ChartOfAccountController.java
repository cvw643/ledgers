package de.adorsys.ledgers.rest.controller;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.rest.api.controller.ChartOfAccountRestApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.security.Principal;

import static de.adorsys.ledgers.postings.api.exception.PostingErrorCode.CHART_OF_ACCOUNT_NOT_FOUND;

@RestController
@RequestMapping(ChartOfAccountRestApi.BASE_PATH)
@RequiredArgsConstructor
public class ChartOfAccountController implements ChartOfAccountRestApi {
    private static final String COA_NF_BY_ID_MSG = "Chart of Account with %s: %s not found!";
    private static final UriBuilder uri = new DefaultUriBuilderFactory().builder();

    private final Principal principal;
    private final ChartOfAccountService chartOfAccountService;

    @Override
    public ResponseEntity<Void> newChartOfAccount(ChartOfAccountBO chartOfAccount) {
        chartOfAccount.setUserDetails(principal.getName());
        ChartOfAccountBO coa = chartOfAccountService.newChartOfAccount(chartOfAccount);
        URI location = uri.path(coa.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<ChartOfAccountBO> findChartOfAccountsById(String id) {
        ChartOfAccountBO coa = chartOfAccountService.findChartOfAccountsById(id)
                                       .orElseThrow(() -> PostingModuleException.builder()
                                                                  .errorCode(CHART_OF_ACCOUNT_NOT_FOUND)
                                                                  .devMsg(String.format(COA_NF_BY_ID_MSG, "id", id))
                                                                  .build());
        return ResponseEntity.ok(coa);
    }

    /**
     * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
     *
     * @param name the name of chart of account
     * @return an empty list if no chart of account with the given name.
     */
    @Override
    public ResponseEntity<ChartOfAccountBO> findChartOfAccountsByName(String name) {
        ChartOfAccountBO coa = chartOfAccountService.findChartOfAccountsByName(name)
                                       .orElseThrow(() -> PostingModuleException.builder()
                                                                  .errorCode(CHART_OF_ACCOUNT_NOT_FOUND)
                                                                  .devMsg(String.format(COA_NF_BY_ID_MSG, "name", name))
                                                                  .build());
        return ResponseEntity.ok(coa);
    }
}
