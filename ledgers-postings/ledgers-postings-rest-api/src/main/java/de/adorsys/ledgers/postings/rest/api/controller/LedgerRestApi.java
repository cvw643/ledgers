package de.adorsys.ledgers.postings.rest.api.controller;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG009 - Mock-PostingData-Upload")
public interface LedgerRestApi {
    String BASE_PATH = "/posting";

    @PostMapping(path = "/ledgers")
    @ApiOperation(value = "Creates new Ledger.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> newLedger(@RequestBody LedgerBO ledger);

    @GetMapping(path = "/ledgers/{id}")
    @ApiOperation(value = "Finds ledger by it identifier.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<LedgerBO> findLedgerById(@PathVariable("id") String id);

    @GetMapping(path = "/ledgers", params = {"ledgerName"})
    @ApiOperation(value = "Finds ledger by its name.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<LedgerBO> findLedgerByName(@RequestParam(name = "ledgerName") String ledgerName);

    @PostMapping(path = "/accounts")
    @ApiOperation(value = "Create a new Ledger Account.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> newLedgerAccount(@RequestBody LedgerAccountBO ledgerAccount);

    @GetMapping(path = "/accounts/{id}")
    @ApiOperation(value = "Find Ledger Account by identifier.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<LedgerAccountBO> findLedgerAccountById(@PathVariable("id") String id);

    @GetMapping(path = "/accounts", params = {"ledgerName", "accountName"})
    @ApiOperation(value = "Find Ledger Account by name.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<LedgerAccountBO> findLedgerAccountByName(@RequestParam(name = "ledgerName") String ledgerName, @RequestParam(name = "accountName") String accountName);

    @GetMapping(path = "/ledgers/{ledgerId}/accounts", params = {"accountName"})
    @ApiOperation(value = "Find Ledger Account by LEdger identifier and account name.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<LedgerAccountBO> findLedgerAccount(@PathVariable("ledgerId") String ledgerId, @RequestParam(name = "accountName") String accountName);
}
