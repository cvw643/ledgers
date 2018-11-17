package de.adorsys.ledgers.middleware.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.converter.SCAMethodTOConverter;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.service.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTypeTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.AuthCodeGenerationMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.service.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.sca.exception.AuthCodeGenerationException;
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import pro.javatar.commons.reader.YamlReader;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";
    private static final int VALIDITY_SECONDS = 60;
    private static final String ACCOUNT_ID = "id";

    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String USER_MESSAGE = "user message";
    private static final String IBAN = "DE91100000000123456789";

    private static final LocalDateTime TIME = LocalDateTime.now();
    
    @InjectMocks
    private MiddlewareServiceImpl middlewareService;

    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private SCAOperationService operationService;
    @Mock
    private PaymentConverter paymentConverter;
    @Mock
    private DepositAccountService accountService;
    @Mock
    private AccountDetailsMapper detailsMapper;

    @Mock
    private UserService userService;

    @Mock
    private SCAMethodTOConverter scaMethodTOConverter;

    private ScaUserDataBO userDataBO;
    private SCAMethodTO scaMethodTO;

    @Before
    public void setUp() {
        userDataBO = new ScaUserDataBO();
        scaMethodTO = new SCAMethodTO();
    }

    @Test
    public void updateScaMethods() throws UserNotFoundException, UserNotFoundMiddlewareException {
        String userLogin = "userLogin";
        List<SCAMethodTO> scaMethodTOS = getDataFromFile("SCAMethodTO.yml", new TypeReference<List<SCAMethodTO>>() {
        });
        List<ScaUserDataBO> userData = getDataFromFile("SCAUserDataBO.yml", new TypeReference<List<ScaUserDataBO>>() {
        });

        when(scaMethodTOConverter.toSCAMethodListBO(scaMethodTOS)).thenReturn(userData);
        when(userService.updateScaData(userData, userLogin)).thenReturn(new UserBO());

        middlewareService.updateScaMethods(scaMethodTOS, userLogin);

        verify(userService, times(1)).updateScaData(userData, userLogin);
    }

    @Test(expected = UserNotFoundMiddlewareException.class)
    public void updateScaMethodsUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException {
        String userLogin = "userLogin";
        List<SCAMethodTO> scaMethodTOS = getDataFromFile("SCAMethodTO.yml", new TypeReference<List<SCAMethodTO>>() {
        });
        List<ScaUserDataBO> userData = getDataFromFile("SCAUserDataBO.yml", new TypeReference<List<ScaUserDataBO>>() {
        });

        when(scaMethodTOConverter.toSCAMethodListBO(scaMethodTOS)).thenReturn(userData);
        doThrow(UserNotFoundException.class).when(userService).updateScaData(userData, userLogin);


        middlewareService.updateScaMethods(scaMethodTOS, userLogin);

        verify(userService, times(1)).updateScaData(userData, userLogin);
    }

    @Test
    public void getPaymentStatusById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);

        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentStatusByIdWithException() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(new PaymentNotFoundException());

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }


    @Test
    public void generateAuthCode() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException {

        when(scaMethodTOConverter.toScaUserDataBO(scaMethodTO)).thenReturn(userDataBO);
        when(operationService.generateAuthCode(OP_ID, userDataBO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS)).thenReturn(OP_ID);

        String actualOpId = middlewareService.generateAuthCode(OP_ID, scaMethodTO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);

        assertThat(actualOpId, is(OP_ID));

        verify(scaMethodTOConverter, times(1)).toScaUserDataBO(scaMethodTO);
        verify(operationService, times(1)).generateAuthCode(OP_ID, userDataBO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);
    }

    @Test(expected = AuthCodeGenerationMiddlewareException.class)
    public void generateAuthCodeWithException() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException {
        when(scaMethodTOConverter.toScaUserDataBO(scaMethodTO)).thenReturn(userDataBO);
        when(operationService.generateAuthCode(OP_ID, userDataBO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS)).thenThrow(new AuthCodeGenerationException());

        middlewareService.generateAuthCode(OP_ID, scaMethodTO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);
    }

    @Test
    public void validateAuthCode() throws SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        String myAuthCode = "my auth code";

        when(operationService.validateAuthCode(OP_ID, OP_DATA, myAuthCode)).thenReturn(Boolean.TRUE);

        boolean valid = middlewareService.validateAuthCode(OP_ID, OP_DATA, myAuthCode);

        assertThat(valid, is(Boolean.TRUE));

        verify(operationService, times(1)).validateAuthCode(OP_ID, OP_DATA, myAuthCode);
    }

    @Test(expected = SCAOperationNotFoundMiddlewareException.class)
    public void validateAuthCodeWithNotFoundException() throws SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        String myAuthCode = "my auth code";

        when(operationService.validateAuthCode(OP_ID, OP_DATA, myAuthCode)).thenThrow(new SCAOperationNotFoundException());

        middlewareService.validateAuthCode(OP_ID, OP_DATA, myAuthCode);
    }

    @Test(expected = SCAOperationValidationMiddlewareException.class)
    public void validateAuthCodeWithValidationException() throws SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        String myAuthCode = "my auth code";

        when(operationService.validateAuthCode(OP_ID, OP_DATA, myAuthCode)).thenThrow(new SCAOperationValidationException());

        middlewareService.validateAuthCode(OP_ID, OP_DATA, myAuthCode);
    }
    
    @Ignore // TODO dima fix
    @Test
    public void getAccountDetailsByAccountId() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException, IOException, LedgerAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, any(), true)).thenReturn(getDepositAccountDetailsBO());

        when(detailsMapper.toAccountDetailsTO(any(), any())).thenReturn(getAccount(AccountDetailsTO.class));
        AccountDetailsTO details = middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID);

        assertThat(details).isNotNull();
        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID, any(), true);
    }

    @Ignore // TODO dima fix
	@Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_wrong_id() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
    	when(accountService.getDepositAccountById("wrong id", any(), false)).thenThrow(new DepositAccountNotFoundException());
        when(detailsMapper.toAccountDetailsTO(any(), any())).thenReturn(getAccount(AccountDetailsTO.class));

        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID, TIME, false);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        Object result = middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, PAYMENT_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(SinglePaymentTO.class, SINGLE_TO));
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentById_Fail_wrong_id() throws PaymentNotFoundException, PaymentNotFoundMiddlewareException {
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID))
                .thenThrow(new PaymentNotFoundException(WRONG_PAYMENT_ID));
        middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, WRONG_PAYMENT_ID);
    }

    @Test
    public void getSCAMethods() throws UserNotFoundException, UserNotFoundMiddlewareException {
        String login = "spe@adorsys.com.ua";
        List<ScaUserDataBO> userData = getDataFromFile("SCAUserDataBO.yml", new TypeReference<List<ScaUserDataBO>>() {
        });
        List<SCAMethodTO> scaMethodTOS = getDataFromFile("SCAMethodTO.yml", new TypeReference<List<SCAMethodTO>>() {
        });

        UserBO userBO = new UserBO();
        userBO.getScaUserData().addAll(userData);
        when(userService.findByLogin(login)).thenReturn(userBO);
        when(scaMethodTOConverter.toSCAMethodListTO(userData)).thenReturn(scaMethodTOS);

        List<SCAMethodTO> scaMethods = middlewareService.getSCAMethods(login);

        assertThat(scaMethods.size(), is(2));

        assertThat(scaMethods.get(0).getType(), is(SCAMethodTypeTO.EMAIL));
        assertThat(scaMethods.get(0).getValue(), is("spe@adorsys.com.ua"));

        assertThat(scaMethods.get(1).getType(), is(SCAMethodTypeTO.MOBILE));
        assertThat(scaMethods.get(1).getValue(), is("+380933686868"));

        verify(userService, times(1)).findByLogin(login);
        verify(scaMethodTOConverter, times(1)).toSCAMethodListTO(userData);
    }

    @Test(expected = UserNotFoundMiddlewareException.class)
    public void getSCAMethodsUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException {
        String login = "spe@adorsys.com.ua";

        when(userService.findByLogin(login)).thenThrow(new UserNotFoundException());

        middlewareService.getSCAMethods(login);
    }

    @Test
    public void initiatePayment() throws AccountNotFoundMiddlewareException {
        when(paymentConverter.toPaymentBO(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentService.initiatePayment(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));

        Object result = middlewareService.initiatePayment(readYml(SinglePaymentTO.class, SINGLE_TO), PaymentTypeTO.SINGLE);
        assertThat(result).isNotNull();
    }

    @Ignore // TODO dima fix
    @Test
    public void getBalances_Success() throws AccountNotFoundMiddlewareException, LedgerAccountNotFoundException, IOException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, any(), any())).thenReturn(getDepositAccountDetailsBO());

        List<AccountBalanceTO> balances = middlewareService.getBalances(ACCOUNT_ID);
        assertThat(balances).isNotEmpty();
        assertThat(balances.size()).isEqualTo(2);
    }

    @Ignore // TODO dima fix
    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getBalances_Failure_depositAccount_Not_Found() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID, any(), any())).thenThrow(new DepositAccountNotFoundException());
        middlewareService.getBalances(ACCOUNT_ID);
    }

    @Ignore // TODO dima fix
    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getBalances_Failure_ledgerAccount_Not_Found() throws AccountNotFoundMiddlewareException, LedgerAccountNotFoundException, DepositAccountNotFoundException {

        middlewareService.getBalances(ACCOUNT_ID);
    }

    @Test
    public void executePayment_Success() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
        when(paymentService.executePayment(any())).thenReturn(TransactionStatusBO.ACSP);

        TransactionStatusTO result = middlewareService.executePayment(PAYMENT_ID);
        assertThat(result).isNotNull();
    }

    @Test(expected = PaymentProcessingMiddlewareException.class)
    public void executePayment_Failure() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
        when(paymentService.executePayment(any())).thenThrow(new PaymentNotFoundException());

        middlewareService.executePayment(PAYMENT_ID);
    }

    @Test
    public void getAllAccountDetailsByUserLogin() throws UserNotFoundMiddlewareException, UserNotFoundException, DepositAccountNotFoundException, AccountNotFoundMiddlewareException {

        String userLogin = "spe";

        AccountDetailsTO account = new AccountDetailsTO();
        DepositAccountDetailsBO accountBO = getDepositAccountDetailsBO();

        List<AccountAccessBO> accessBOList = getDataFromFile("account-access-bo-list.yml", new TypeReference<List<AccountAccessBO>>() {
        });
        String iban = accessBOList.get(0).getIban();

        UserBO userBO = new UserBO();
        userBO.getAccountAccesses().addAll(accessBOList);
        when(userService.findByLogin(userLogin)).thenReturn(userBO);
        
        when(accountService.getDepositAccountsByIBAN(Collections.singletonList(iban), LocalDateTime.MIN, false)).thenReturn(Collections.singletonList(accountBO));
        when(detailsMapper.toAccountDetailsTO(accountBO)).thenReturn(account);

        List<AccountDetailsTO> details = middlewareService.getAllAccountDetailsByUserLogin(userLogin);

        assertThat(details.size(), is(1));
        assertThat(details.get(0), is(account));

        verify(userService, times(1)).findByLogin(userLogin);
        verify(accountService, times(1)).getDepositAccountsByIBAN(Collections.singletonList(iban), LocalDateTime.MIN, false);
        verify(detailsMapper, times(1)).toAccountDetailsTO(accountBO);
    }

    private static <T> T getAccount(Class<T> aClass) {
        try {
            return YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/AccountDetails.yml", aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
    
    private DepositAccountDetailsBO getDepositAccountDetailsBO() {
        try {
            return YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/DepositAccountDetailsBO.yml", DepositAccountDetailsBO.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
	}
    

    @Test
    public void getTransactionById() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenReturn(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        when(paymentConverter.toTransactionTO(any())).thenReturn(readYml(TransactionTO.class, "TransactionTO.yml"));

        TransactionTO result = middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test(expected = TransactionNotFoundMiddlewareException.class)
    public void getTransactionById_Failure() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenThrow(new TransactionNotFoundException("ACCOUNT_ID", "POSTING_ID"));

        middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
    }

    @Test
    public void getAccountDetailsByIban() throws LedgerAccountNotFoundException, DepositAccountNotFoundException, IOException, AccountNotFoundMiddlewareException {
        DepositAccountDetailsBO accountBO = getDepositAccountDetailsBO();
        AccountDetailsTO accountDetailsTO = getAccount(AccountDetailsTO.class);

        when(accountService.getDepositAccountByIBAN(IBAN, TIME, true)).thenReturn(accountBO);
        when(detailsMapper.toAccountDetailsTO(accountBO)).thenReturn(accountDetailsTO);
        AccountDetailsTO details = middlewareService.getAccountDetailsWithBalancesByIban(IBAN, TIME);

        assertThat(details).isNotNull();
        assertThat(details, is(accountDetailsTO));

        verify(accountService, times(1)).getDepositAccountByIBAN(IBAN, TIME, true);
        verify(detailsMapper, times(1)).toAccountDetailsTO(accountBO);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByIbanDepositAccountNotFoundException() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {

        when(accountService.getDepositAccountByIBAN(IBAN, TIME, true)).thenThrow(new DepositAccountNotFoundException());

        middlewareService.getAccountDetailsWithBalancesByIban(IBAN, TIME);
        verify(accountService, times(1)).getDepositAccountByIBAN(IBAN, TIME, true);
    }

    //    todo: replace by javatar-commons version 0.7
    private <T> T getDataFromFile(String fileName, TypeReference<T> typeReference) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void getTransactionsByDates() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionsByDates(any(), any(), any())).thenReturn(Collections.singletonList(new TransactionDetailsBO()));
        when(paymentConverter.toTransactionTOList(any())).thenReturn(Collections.singletonList(new TransactionTO()));
        List<TransactionTO> result = middlewareService.getTransactionsByDates(ACCOUNT_ID, LocalDate.of(2018, 12, 12), LocalDate.of(2018, 12, 18));
        assertThat(result.isEmpty()).isFalse();
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getTransactionsByDates_Failure() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getTransactionsByDates(any(), any(), any())).thenThrow(new DepositAccountNotFoundException());
        middlewareService.getTransactionsByDates(ACCOUNT_ID, LocalDate.of(2018, 12, 12), LocalDate.of(2018, 12, 18));
    }

}