package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DepositAccountService {

    DepositAccountBO createDepositAccount(DepositAccountBO depositAccount, String userName);

    DepositAccountBO createDepositAccountForBranch(DepositAccountBO depositAccount, String userName, String branch);

    DepositAccountDetailsBO getDepositAccountByIbanAndCheckStatus(String iban, LocalDateTime refTime, boolean withBalances);

    DepositAccountDetailsBO getDepositAccountByIban(String iban, LocalDateTime refTime, boolean withBalances);

    List<DepositAccountDetailsBO> getDepositAccountsByIban(List<String> ibans, LocalDateTime refTime, boolean withBalances);

    DepositAccountDetailsBO getDepositAccountById(String accountId, LocalDateTime refTime, boolean withBalances);

    TransactionDetailsBO getTransactionById(String accountId, String transactionId);

    List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo);

    Page<TransactionDetailsBO> getTransactionsByDatesPaged(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO);

    String readIbanById(String id);

    List<DepositAccountBO> findByAccountNumberPrefix(String accountNumberPrefix);

    List<DepositAccountDetailsBO> findByBranch(String branch);

    Page<DepositAccountDetailsBO> findByBranchPaged(String branch, Pageable pageable);

    void depositCash(String accountId, AmountBO amount, String user);

    void deleteTransactions(String iban);

    void deleteBranch(String branchId);
}
