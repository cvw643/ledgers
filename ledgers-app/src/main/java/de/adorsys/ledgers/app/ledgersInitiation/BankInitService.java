package de.adorsys.ledgers.app.ledgersInitiation;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.NoAccessMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentWithIdMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.mockbank.simple.data.BulkPaymentsData;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.SinglePaymentsData;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class BankInitService {
    private final MockbankInitData mockbankInitData;
    private final UserService userService;
    private final Environment env;
    private final UserMapper userMapper;
    private final Logger logger = LoggerFactory.getLogger(BankInitService.class);
    private final DepositAccountInitService depositAccountInitService;
    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final MiddlewarePaymentService paymentService;

    @Autowired
    public BankInitService(MockbankInitData mockbankInitData, UserService userService, Environment env, UserMapper userMapper, DepositAccountInitService depositAccountInitService, DepositAccountService depositAccountService, AccountDetailsMapper accountDetailsMapper, MiddlewarePaymentService paymentService) {
        this.mockbankInitData = mockbankInitData;
        this.userService = userService;
        this.env = env;
        this.userMapper = userMapper;
        this.depositAccountInitService = depositAccountInitService;
        this.depositAccountService = depositAccountService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.paymentService = paymentService;
    }

    public void init() {
        depositAccountInitService.initConfigData();
        if (Arrays.asList(this.env.getActiveProfiles()).contains("develop")) {
            uploadTestData();
        }
    }

    private void uploadTestData() {
        createUsers();
        createAccounts();
        performTransactions();
    }

    private void performTransactions() {
        performSinglePayments();
        performBulkPayments();
    }

    private void performSinglePayments() {
        for (SinglePaymentsData paymentsData : mockbankInitData.getSinglePayments()) {
            try {
                if (transactionIsAbsent(paymentsData.getSinglePayment())) {
                    paymentService.initiatePayment(paymentsData.getSinglePayment(), PaymentTypeTO.SINGLE);
                }
            } catch (AccountNotFoundMiddlewareException | NoAccessMiddlewareException |
                             PaymentWithIdMiddlewareException | DepositAccountNotFoundException e) {
                logger.error("Account not Found! Should never happen while initiating mock data!");
            }
        }
    }
    private void performBulkPayments() {
        for (BulkPaymentsData paymentsData : mockbankInitData.getBulkPayments()) {
            try {
                if (transactionIsAbsent(paymentsData.getBulkPayment().getPayments().get(0))) {
                    paymentService.initiatePayment(paymentsData.getBulkPayment(), PaymentTypeTO.SINGLE);
                }
            } catch (AccountNotFoundMiddlewareException | NoAccessMiddlewareException |
                             PaymentWithIdMiddlewareException | DepositAccountNotFoundException e) {
                logger.error("Account not Found! Should never happen while initiating mock data!");
            }
        }
    }

    private boolean transactionIsAbsent(SinglePaymentTO payment) throws DepositAccountNotFoundException {
        DepositAccountDetailsBO account = depositAccountService.getDepositAccountByIban(payment.getDebtorAccount().getIban(), LocalDateTime.now(), false);
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getAccount().getId(), payment.getRequestedExecutionDate().atStartOfDay(), payment.getRequestedExecutionDate().plusDays(1).atStartOfDay());
        return transactions.stream().noneMatch(t -> t.getEndToEndId().equals(payment.getEndToEndIdentification()));
    }

    private void createAccounts() {
        for (AccountDetailsTO details : mockbankInitData.getAccounts()) {
            try {
                DepositAccountDetailsBO account = depositAccountService.getDepositAccountByIban(details.getIban(), LocalDateTime.now(), false);
                if (account == null) {
                    String userName = getUserNameByIban(details.getIban());
                    depositAccountService.createDepositAccount(accountDetailsMapper.toDepositAccountBO(details), userName);
                }
            } catch (DepositAccountNotFoundException e) {
                logger.error("Account not Found! Should never happen while initiating mock data!");
            } catch (UserNotFoundException e) {
                logger.error("User for account {} not found! Should never happen while initiating mock data!", details.getIban());
            }
        }
    }

    private String getUserNameByIban(String iban) throws UserNotFoundException {
        return mockbankInitData.getUsers().stream()
                       .filter(u -> isAccountContainedInAccess(u.getAccountAccesses(), iban))
                       .findFirst()
                       .map(UserTO::getLogin)
                       .orElseThrow(UserNotFoundException::new);
    }

    private boolean isAccountContainedInAccess(List<AccountAccessTO> access, String iban) {
        return access.stream()
                       .anyMatch(a -> a.getIban().equals(iban));
    }

    private void createUsers() {
        for (UserTO user : mockbankInitData.getUsers()) {
            try {
                userService.findByLogin(user.getLogin());
            } catch (UserNotFoundException e) {
                createUser(user);
            }
        }
    }

    private void createUser(UserTO user) {
        try {
            userService.create(userMapper.toUserBO(user));
        } catch (UserAlreadyExistsException e1) {
            logger.error("User already exists! Should never happen while initiating mock data!");
        }
    }
}
