package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface DepositAccountRepository extends PagingAndSortingRepository<DepositAccount, String> {
    List<DepositAccount> findByIbanIn(List<String> ibans);

    List<DepositAccount> findByIbanStartingWith(String iban);  //TODO fix this!

    List<DepositAccount> findByBranch(String branch);

    Page<DepositAccount> findByBranch(String branch, Pageable pageable);

    Optional<DepositAccount> findByIbanAndCurrency(String iban, String currency);

}
