package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;

import static de.adorsys.ledgers.postings.api.exception.ExceptionCode.*;

@RequiredArgsConstructor
public class AbstractServiceImpl {
    private static final String COA_NF_BY_ID_MSG = "Chart of Account with id: %s not found!";
    private static final String COA_NF_BY_NAME_MSG = "Chart of Account with name: %s not found!";
    private static final String LA_NF_BY_ID_MSG = "Ledger Account with id: %s not found!";
    protected static final String LA_NF_BY_NAME_MSG = "Ledger Account with Ledger name : %s not found!";
    private static final String LEDGER_NF_BY_ID_MSG = "Ledger with id: %s not found!";
    private static final String LEDGER_NF_BY_NAME_MSG = "Ledger with Ledger name : %s not found!";

    protected final LedgerAccountRepository ledgerAccountRepository;
    protected final ChartOfAccountRepository chartOfAccountRepo;
    protected final LedgerRepository ledgerRepository;
    protected final LedgerAccountMapper ledgerAccountMapper = Mappers.getMapper(LedgerAccountMapper.class);

    protected ChartOfAccount loadCoa(ChartOfAccountBO model) {
        if (model == null) {
            throw nullInfo();
        }
        if (model.getId() != null) {
            return chartOfAccountRepo.findById(model.getId())
                           .orElseThrow(() -> new PostingModuleException(CHART_OF_ACCOUNT_NOT_FOUND, String.format(COA_NF_BY_ID_MSG, model.getId())));
        }
        if (model.getName() != null) {
            return chartOfAccountRepo.findOptionalByName(model.getName())
                           .orElseThrow(() -> new PostingModuleException(CHART_OF_ACCOUNT_NOT_FOUND, String.format(COA_NF_BY_NAME_MSG, model.getName())));
        }
        throw insufficientInfo(model);
    }

    /*
     * Load the ledger account. Using the following logic in the given order. 1-
     * If the Id is provided, we use find by id. 2- If the ledger and the name
     * is provided, we use them to load the account.
     */
    protected LedgerAccount loadLedgerAccount(LedgerAccountBO model) {
        LedgerAccount ledgerAccount = ledgerAccountMapper.toLedgerAccount(model);
        return loadLedgerAccount(ledgerAccount);
    }

    protected LedgerAccount loadLedgerAccount(LedgerAccount model) {
        if (model == null) {
            throw nullInfo();
        }
        if (model.getId() != null) {
            return ledgerAccountRepository.findById(model.getId())
                           .orElseThrow(() -> new PostingModuleException(LEDGER_ACCOUNT_NOT_FOUND, String.format(LA_NF_BY_ID_MSG, model.getId())));
        }
        if (model.getLedger() != null && model.getName() != null) {
            Ledger loadedLedger = loadLedger(model.getLedger());
            return ledgerAccountRepository.findOptionalByLedgerAndName(loadedLedger, model.getName())
                           .orElseThrow(() -> new PostingModuleException(LEDGER_ACCOUNT_NOT_FOUND, String.format(LA_NF_BY_NAME_MSG, model.getName())));
        }
        throw insufficientInfo(model);
    }

    protected Ledger loadLedger(LedgerBO model) {
        if (model == null) {
            throw nullInfo();
        }
        return loadLedgerByIdOrName(model.getId(), model.getName());
    }

    protected Ledger loadLedger(Ledger model) {
        if (model == null) {
            throw nullInfo();
        }
        return loadLedgerByIdOrName(model.getId(), model.getName());
    }

    private Ledger loadLedgerByIdOrName(String id, String name) {
        if (id != null) {
            return ledgerRepository.findById(id)
                           .orElseThrow(() -> new PostingModuleException(LEDGER_NOT_FOUND, String.format(LEDGER_NF_BY_ID_MSG, id)));
        }
        if (name != null) {
            return ledgerRepository.findOptionalByName(name)
                           .orElseThrow(() -> new PostingModuleException(LEDGER_NOT_FOUND, String.format(LEDGER_NF_BY_NAME_MSG, name)));
        }
        throw insufficientInfo("Both id and name fields are NULL!");
    }

    protected PostingModuleException insufficientInfo(Object modelObject) {
        return new PostingModuleException(NULL_MODEL,
                String.format("Model Object does not provide sufficient information for loading original instance. %s",
                        modelObject.toString()));
    }

    private PostingModuleException nullInfo() {
        return new PostingModuleException(NULL_MODEL, "Model object can not be null");
    }
}
