package de.adorsys.ledgers.rest.controller;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.rest.api.controller.LedgerRestApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.security.Principal;

import static de.adorsys.ledgers.postings.api.exception.PostingErrorCode.LEDGER_ACCOUNT_NOT_FOUND;
import static de.adorsys.ledgers.postings.api.exception.PostingErrorCode.LEDGER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping(LedgerRestApi.BASE_PATH)
public class LedgerController implements LedgerRestApi {
    private static final String LEDGER_NF_MSG = "Ledger with %s: %s not found!";
    private static final String LA_NF_MSG = "Ledger Account with %s: %s not found!";
    private static final UriBuilder uri = new DefaultUriBuilderFactory().builder();

    private final Principal principal;
    private final LedgerService ledgerService;

    /**
     * Creates a new Ledger.
     *
     * @param ledger Ledger object
     * @return void response with HttpStatus 201 if successful
     */
    @Override
    public ResponseEntity<Void> newLedger(LedgerBO ledger) {
        LedgerBO newLedger = ledgerService.newLedger(ledger);

        URI location = uri.path(newLedger.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<LedgerBO> findLedgerById(String id) {
        LedgerBO ledger = ledgerService.findLedgerById(id)
                                  .orElseThrow(() -> PostingModuleException.builder()
                                                             .errorCode(LEDGER_NOT_FOUND)
                                                             .devMsg(String.format(LEDGER_NF_MSG, "id", id))
                                                             .build());
        return ResponseEntity.ok(ledger);
    }

    /**
     * Find the ledger with the given name.
     *
     * @param ledgerName name of corresponding Ledger
     * @return Ledger object
     */
    @Override
    public ResponseEntity<LedgerBO> findLedgerByName(String ledgerName) {
        LedgerBO ledger = ledgerService.findLedgerByName(ledgerName)
                                  .orElseThrow(() -> PostingModuleException.builder()
                                                             .errorCode(LEDGER_NOT_FOUND)
                                                             .devMsg(String.format(LEDGER_NF_MSG, "name", ledgerName))
                                                             .build());
        return ResponseEntity.ok(ledger);
    }

    /**
     * Create a new Ledger account.
     * <p>
     * While creating a ledger account, the parent hat to be specified.
     *
     * @param ledgerAccount Ledger account
     * @return Void response with 201 HttpStatus if successful
     */
    @Override
    public ResponseEntity<Void> newLedgerAccount(LedgerAccountBO ledgerAccount) {
        LedgerAccountBO newLedgerAccount = ledgerService.newLedgerAccount(ledgerAccount, principal.getName());
        URI location = uri.path(newLedgerAccount.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<LedgerAccountBO> findLedgerAccountById(String id) {
        LedgerAccountBO la = ledgerService.findLedgerAccountById(id)
                                     .orElseThrow(() -> PostingModuleException.builder()
                                                                .errorCode(LEDGER_ACCOUNT_NOT_FOUND)
                                                                .devMsg(String.format(LA_NF_MSG, "id", id))
                                                                .build());
        return ResponseEntity.ok(la);
    }

    /**
     * Find the ledger account with the given ledger name and account name and reference date.
     *
     * @param ledgerName  name of ledger
     * @param accountName name of account
     * @return Ledger Account
     */
    @Override
    public ResponseEntity<LedgerAccountBO> findLedgerAccountByName(String ledgerName, String accountName) {
        LedgerBO ledger = new LedgerBO();
        ledger.setName(ledgerName);

        return ledgerAccount(ledger, accountName);
    }

    /**
     * Find the ledger account with the given name
     *
     * @param accountName name of corresponding account
     * @return Ledger account
     */
    @Override
    public ResponseEntity<LedgerAccountBO> findLedgerAccount(String ledgerId, String accountName) {
        LedgerBO ledger = new LedgerBO();
        ledger.setId(ledgerId);
        return ledgerAccount(ledger, accountName);
    }

    private ResponseEntity<LedgerAccountBO> ledgerAccount(LedgerBO ledger, String accountName) {
        LedgerAccountBO ledgerAccount = ledgerService.findLedgerAccount(ledger, accountName);

        return ResponseEntity.ok(ledgerAccount);
    }
}
