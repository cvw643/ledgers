package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.deposit.api.domain.AccountStatusBO.ENABLED;
import static de.adorsys.ledgers.deposit.api.domain.BalanceTypeBO.*;
import static de.adorsys.ledgers.util.exception.DepositErrorCode.*;
import static java.lang.String.format;

@Slf4j
@Service
@SuppressWarnings("PMD.TooManyMethods")
public class DepositAccountServiceImpl extends AbstractServiceImpl implements DepositAccountService {
    private static final String MSG_IBAN_NOT_FOUND = "Accounts with iban %s not found";
    private static final String OPERATION_ON_BLOCKED_ACCOUNT = "Operation is Rejected as account: %s is %s";
    private static final String DELETE_BRANCH_ERROR_MSG = "Something went wrong during deletion of branch: %s, msg: %s";
    private static final String BRANCH_SQL = "classpath:deleteBranch.sql";
    private static final String POSTING_SQL = "classpath:deletePostings.sql";
    private static final String DELETE_POSTINGS_ERROR_MSG = "Something went wrong during deletion of postings for iban: %s, msg: %s";

    @PersistenceContext
    private final EntityManager entityManager;
    private final ResourceLoader loader;
    private final DepositAccountRepository depositAccountRepository;
    private final DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);
    private final AccountStmtService accountStmtService;
    private final PostingService postingService;
    private final TransactionDetailsMapper transactionDetailsMapper;
    private final ObjectMapper objectMapper;

    public DepositAccountServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                     LedgerService ledgerService, EntityManager entityManager, ResourceLoader loader, DepositAccountRepository depositAccountRepository,
                                     AccountStmtService accountStmtService,
                                     PostingService postingService, TransactionDetailsMapper transactionDetailsMapper,
                                     ObjectMapper objectMapper) {
        super(depositAccountConfigService, ledgerService);
        this.entityManager = entityManager;
        this.loader = loader;
        this.depositAccountRepository = depositAccountRepository;
        this.accountStmtService = accountStmtService;
        this.postingService = postingService;
        this.transactionDetailsMapper = transactionDetailsMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public DepositAccountBO createDepositAccount(DepositAccountBO depositAccountBO, String userName) {
        checkDepositAccountAlreadyExist(depositAccountBO);
        DepositAccount da = createDepositAccountObj(depositAccountBO, userName);
        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    @Override
    public DepositAccountBO createDepositAccountForBranch(DepositAccountBO depositAccountBO, String userName, String branch) {
        checkDepositAccountAlreadyExist(depositAccountBO);
        DepositAccount da = createDepositAccountObj(depositAccountBO, userName);
        da.setBranch(branch);
        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    @Override
    public DepositAccountDetailsBO getDepositAccountByIbanAndCheckStatus(String iban, LocalDateTime refTime, boolean withBalances) {
        DepositAccountDetailsBO account = getDepositAccountByIban(iban, refTime, withBalances);
        AccountStatusBO accountStatus = account.getAccount().getAccountStatus();
        if (accountStatus != ENABLED) {
            throw DepositModuleException.builder()
                          .errorCode(ACCOUNT_BLOCKED_DELETED)
                          .devMsg(format(OPERATION_ON_BLOCKED_ACCOUNT, account.getAccount().getIban(), accountStatus))
                          .build();
        }
        return account;
    }

    @Override
    public DepositAccountDetailsBO getDepositAccountByIban(String iban, LocalDateTime refTime, boolean withBalances) {
        List<DepositAccountBO> accounts = getDepositAccountsByIban(Collections.singletonList(iban));

        if (accounts.isEmpty()) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                          .devMsg(format(MSG_IBAN_NOT_FOUND, iban))
                          .build();
        }
        DepositAccountBO account = accounts.iterator().next();
        return new DepositAccountDetailsBO(account, getBalancesList(account, withBalances, refTime));
    }

    @Override
    public List<DepositAccountDetailsBO> getDepositAccountsByIban(List<String> ibans, LocalDateTime refTime, boolean withBalances) {
        List<DepositAccountDetailsBO> result = new ArrayList<>();
        for (String iban : ibans) {
            result.add(getDepositAccountByIban(iban, refTime, withBalances));
        }
        return result;
    }

    @Override
    public DepositAccountDetailsBO getDepositAccountById(String accountId, LocalDateTime refTime, boolean withBalances) {
        DepositAccountBO depositAccountBO = getDepositAccountById(accountId);
        return new DepositAccountDetailsBO(depositAccountBO, getBalancesList(depositAccountBO, withBalances, refTime));
    }

    @Override
    public TransactionDetailsBO getTransactionById(String accountId, String transactionId) {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerBO ledgerBO = loadLedger();
        LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccount(ledgerBO, account.getIban());
        PostingLineBO line = postingService.findPostingLineById(ledgerAccountBO, transactionId);
        return transactionDetailsMapper.toTransactionSigned(line);
    }

    @Override
    public List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerBO ledgerBO = loadLedger();
        LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccount(ledgerBO, account.getIban());
        return postingService.findPostingsByDates(ledgerAccountBO, dateFrom, dateTo)
                       .stream()
                       .map(transactionDetailsMapper::toTransactionSigned)
                       .collect(Collectors.toList());
    }

    @Override
    public Page<TransactionDetailsBO> getTransactionsByDatesPaged(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerBO ledgerBO = loadLedger();
        LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccount(ledgerBO, account.getIban());

        return postingService.findPostingsByDatesPaged(ledgerAccountBO, dateFrom, dateTo, pageable)
                       .map(transactionDetailsMapper::toTransactionSigned);
    }

    @Override
    public boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO) {
        DepositAccountDetailsBO account = getDepositAccountByIban(requestBO.getPsuAccount().getIban(), LocalDateTime.now(), true);
        return account.getBalances().stream()
                       .filter(b -> b.getBalanceType() == INTERIM_AVAILABLE)
                       .findFirst()
                       .map(b -> isSufficientAmountAvailable(requestBO, b))
                       .orElse(Boolean.FALSE);
    }

    @Override
    public String readIbanById(String id) {
        return depositAccountRepository.findById(id).map(DepositAccount::getIban).orElse(null);
    }

    @Override
    public List<DepositAccountBO> findByAccountNumberPrefix(String accountNumberPrefix) {
        List<DepositAccount> accounts = depositAccountRepository.findByIbanStartingWith(accountNumberPrefix);
        return depositAccountMapper.toDepositAccountListBO(accounts);
    }

    @Override
    public List<DepositAccountDetailsBO> findByBranch(String branch) {
        List<DepositAccount> accounts = depositAccountRepository.findByBranch(branch);
        List<DepositAccountBO> accountsBO = depositAccountMapper.toDepositAccountListBO(accounts);
        List<DepositAccountDetailsBO> accountDetails = new ArrayList<>();
        for (DepositAccountBO accountBO : accountsBO) {
            accountDetails.add(new DepositAccountDetailsBO(accountBO, Collections.emptyList()));
        }
        return accountDetails;
    }

    @Override
    public Page<DepositAccountDetailsBO> findByBranchPaged(String branch, Pageable pageable) {
        return depositAccountRepository.findByBranch(branch, pageable)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .map(d -> new DepositAccountDetailsBO(d, Collections.emptyList()));
    }

    @Override
    public void depositCash(String accountId, AmountBO amount, String recordUser) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg("Deposited amount must be greater than zero")
                          .build();
        }

        DepositAccount depositAccount = getDepositAccountEntityById(accountId);
        AccountReferenceBO accountReference = depositAccountMapper.toAccountReferenceBO(depositAccount);
        if (!accountReference.getCurrency().equals(amount.getCurrency())) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg(format("Deposited amount and account currencies are different. Requested currency: %s, Account currency: %s",
                                         amount.getCurrency().getCurrencyCode(), accountReference.getCurrency().getCurrencyCode()))
                          .build();
        }

        LedgerBO ledger = loadLedger();
        LocalDateTime postingDateTime = LocalDateTime.now();

        depositCash(accountReference, amount, recordUser, ledger, postingDateTime);
    }

    @Override
    public void deleteTransactions(String iban) {
        getDepositAccountByIban(iban, LocalDateTime.now(), false);
        LedgerBO ledger = loadLedger();
        String accountId = ledgerService.findLedgerAccount(ledger, iban).getId();
        executeNativeQuery(POSTING_SQL, accountId, DELETE_POSTINGS_ERROR_MSG);
    }

    @Override
    public void deleteBranch(String branchId) {
        executeNativeQuery(BRANCH_SQL, branchId, DELETE_BRANCH_ERROR_MSG);
    }

    private void executeNativeQuery(String queryFilePath, String parameter, String errorMsg) {
        try {
            InputStream stream = loader.getResource(queryFilePath).getInputStream();
            String query = IOUtils.toString(stream, StandardCharsets.UTF_8);
            entityManager.createNativeQuery(query)
                    .setParameter(1, parameter)
                    .executeUpdate();
        } catch (IOException e) {
            throw DepositModuleException.builder()
                          .devMsg(format(errorMsg, parameter, e.getMessage()))
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }

    private void checkDepositAccountAlreadyExist(DepositAccountBO depositAccountBO) {
        boolean isExistingAccount = depositAccountRepository.findByIbanAndCurrency(depositAccountBO.getIban(), depositAccountBO.getCurrency().getCurrencyCode())
                                            .isPresent();
        if (isExistingAccount) {
            String message = format("Deposit account already exists. IBAN %s. Currency %s",
                                    depositAccountBO.getIban(), depositAccountBO.getCurrency().getCurrencyCode());
            log.error(message);
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_ACCOUNT_EXISTS)
                          .devMsg(message)
                          .build();
        }
    }

    private DepositAccount createDepositAccountObj(DepositAccountBO depositAccountBO, String userName) {
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);

        LedgerBO ledgerBO = loadLedger();
        String depositParentAccountNbr = depositAccountConfigService.getDepositParentAccount();
        LedgerAccountBO depositParentAccount = new LedgerAccountBO(depositParentAccountNbr, ledgerBO);

        LedgerAccountBO ledgerAccount = new LedgerAccountBO(depositAccount.getIban(), depositParentAccount);

        ledgerService.newLedgerAccount(ledgerAccount, userName);

        return depositAccountMapper.createDepositAccountObj(depositAccount);
    }

    private List<BalanceBO> getBalancesList(DepositAccountBO d, boolean withBalances, LocalDateTime refTime) {
        return withBalances
                       ? getBalances(d.getIban(), d.getCurrency(), refTime)
                       : Collections.emptyList();
    }

    private DepositAccountBO getDepositAccountById(String accountId) {
        return depositAccountMapper.toDepositAccountBO(getDepositAccountEntityById(accountId));
    }

    private DepositAccount getDepositAccountEntityById(String accountId) {
        return depositAccountRepository.findById(accountId)
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                                                  .devMsg(format("Account with id: %s not found!", accountId))
                                                  .build());
    }

    private List<BalanceBO> getBalances(String iban, Currency currency, LocalDateTime refTime) {
        LedgerBO ledger = loadLedger();
        LedgerAccountBO ledgerAccountBO = newLedgerAccountBOObj(ledger, iban);
        return getBalances(currency, refTime, ledgerAccountBO);
    }

    private List<BalanceBO> getBalances(Currency currency, LocalDateTime refTime, LedgerAccountBO ledgerAccountBO) {
        AccountStmtBO stmt = accountStmtService.readStmt(ledgerAccountBO, refTime);
        BalanceBO interimBalance = composeBalance(currency, stmt, INTERIM_AVAILABLE);
        BalanceBO closingBalance = composeBalance(currency, stmt, CLOSING_BOOKED);
        return Arrays.asList(interimBalance, closingBalance);
    }

    private BalanceBO composeBalance(Currency currency, AccountStmtBO stmt, BalanceTypeBO balanceType) {
        BalanceBO balanceBO = new BalanceBO();
        AmountBO amount = new AmountBO(currency, stmt.creditBalance());
        balanceBO.setAmount(amount);
        balanceBO.setBalanceType(balanceType);
        balanceBO.setReferenceDate(stmt.getPstTime().toLocalDate());
        return composeFinalBalance(balanceBO, stmt);
    }

    private BalanceBO composeFinalBalance(BalanceBO balance, AccountStmtBO stmt) {
        PostingTraceBO youngestPst = stmt.getYoungestPst();
        if (youngestPst != null) {
            balance.setLastChangeDateTime(youngestPst.getSrcPstTime());
            balance.setLastCommittedTransaction(youngestPst.getSrcPstId());
        } else {
            balance.setLastChangeDateTime(stmt.getPstTime());
        }
        return balance;
    }

    private List<DepositAccountBO> getDepositAccountsByIban(List<String> ibans) {
        log.info("Retrieving deposit accounts by list of IBANs");

        List<DepositAccount> accounts = depositAccountRepository.findByIbanIn(ibans);
        log.info("{} IBANs were found", accounts.size());

        return depositAccountMapper.toDepositAccountListBO(accounts);
    }

    private boolean isSufficientAmountAvailable(FundsConfirmationRequestBO request, BalanceBO balance) {
        AmountBO balanceAmount = balance.getAmount();
        return Optional.ofNullable(request.getInstructedAmount())
                       .map(r -> balanceAmount.getAmount().compareTo(r.getAmount()) >= 0)
                       .orElse(false);
    }

    private LedgerAccountBO newLedgerAccountBOObj(LedgerBO ledger, String iban) {
        LedgerAccountBO ledgerAccountBO = new LedgerAccountBO();
        ledgerAccountBO.setName(iban);
        ledgerAccountBO.setLedger(ledger);
        return ledgerAccountBO;
    }

    private void depositCash(AccountReferenceBO accountReference, AmountBO amount, String recordUser, LedgerBO ledger, LocalDateTime postingDateTime) {
        PostingBO posting = composePosting(recordUser, ledger, postingDateTime);

        //Debit line
        PostingLineBO debitLine = composeLine(accountReference, amount, ledger, postingDateTime, true);
        posting.getLines().add(debitLine);

        //Credit line
        PostingLineBO creditLine = composeLine(accountReference, amount, ledger, postingDateTime, false);
        posting.getLines().add(creditLine);

        postingService.newPosting(posting);
    }

    private PostingLineBO composeLine(AccountReferenceBO accountReference, AmountBO amount, LedgerBO ledger, LocalDateTime postingDateTime, boolean debit) {
        String cashAccountName = debit
                                         ? depositAccountConfigService.getCashAccount()
                                         : accountReference.getIban();

        LedgerAccountBO account = ledgerService.findLedgerAccount(ledger, cashAccountName);

        BigDecimal debitAmount = debit
                                         ? amount.getAmount()
                                         : BigDecimal.ZERO;

        BigDecimal creditAmount = debit
                                          ? BigDecimal.ZERO
                                          : amount.getAmount();

        String lineId = Ids.id();
        String debitTransactionDetails = newTransactionDetails(amount, accountReference, postingDateTime, lineId);
        return newPostingLine(lineId, account, debitAmount, creditAmount, debitTransactionDetails);
    }

    private PostingBO composePosting(String recordUser, LedgerBO ledger, LocalDateTime postingDateTime) {
        PostingBO posting = new PostingBO();
        posting.setLedger(ledger);
        posting.setPstTime(postingDateTime);
        posting.setOprDetails("Cash Deposit");
        posting.setOprId(Ids.id());
        posting.setPstType(PostingTypeBO.BUSI_TX);
        posting.setRecordUser(recordUser);
        return posting;
    }

    private PostingLineBO newPostingLine(String id, LedgerAccountBO account, BigDecimal debitAmount, BigDecimal creditAmount, String details) {
        PostingLineBO debitLine = new PostingLineBO();
        debitLine.setId(id);
        debitLine.setAccount(account);
        debitLine.setDebitAmount(debitAmount);
        debitLine.setCreditAmount(creditAmount);
        debitLine.setDetails(details);
        return debitLine;
    }

    private String newTransactionDetails(AmountBO amount, AccountReferenceBO creditor, LocalDateTime postingDateTime, String postingLineId) {
        TransactionDetailsBO transactionDetails = new TransactionDetailsBO();
        transactionDetails.setTransactionId(Ids.id());
        transactionDetails.setEndToEndId(postingLineId);
        transactionDetails.setBookingDate(postingDateTime.toLocalDate());
        transactionDetails.setValueDate(postingDateTime.toLocalDate());
        transactionDetails.setTransactionAmount(amount);
        transactionDetails.setCreditorAccount(creditor);
        try {
            return objectMapper.writeValueAsString(transactionDetails);
        } catch (JsonProcessingException e) {
            throw DepositModuleException.builder()
                          .errorCode(PAYMENT_PROCESSING_FAILURE)
                          .devMsg(e.getMessage())
                          .build();
        }
    }
}
