package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.FrequencyCode;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.objectlab.kit.datecalc.common.DateCalculator;
import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import net.objectlab.kit.datecalc.jdk8.LocalDateKitCalculatorsFactory;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static de.adorsys.ledgers.util.exception.DepositErrorCode.PAYMENT_PROCESSING_FAILURE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionService implements InitializingBean {
    private static final String CALENDAR_NAME = "LEDGERS";
    private static final String PRECEDING = "preceding";
    private static final String FOLLOWING = "following";
    private final PaymentRepository paymentRepository;
    private final DepositAccountTransactionService txService;
    private final DepositAccountService accountService;
    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Override
    public void afterPropertiesSet() {
        HolidayCalendar<LocalDate> calendar = new DefaultHolidayCalendar<>(new HashSet<>(readHolidays()));
        LocalDateKitCalculatorsFactory.getDefaultInstance().registerHolidays("DE", calendar);
    }

    public TransactionStatusBO executePayment(Payment payment, String userName) {
        PaymentBO paymentBO = paymentMapper.toPaymentBO(payment);
        AmountBO amountToVerify = calculateTotalPaymentAmount(paymentBO);
        boolean confirmationOfFunds = accountService.confirmationOfFunds(new FundsConfirmationRequestBO(null, paymentBO.getDebtorAccount(), amountToVerify, null, null));

        if (!confirmationOfFunds) {
            updatePaymentStatus(payment, TransactionStatus.RJCT);
            log.info("Scheduler couldn't execute payment : {}. Insufficient funds to complete the operation", payment.getTransactionStatus());
            return TransactionStatusBO.RJCT;
        }
        LocalDateTime executionTime = LocalDateTime.now();
        txService.bookPayment(payment, executionTime, userName);
        payment.setExecutedDate(executionTime);

        if (EnumSet.of(PaymentType.SINGLE, PaymentType.BULK).contains(payment.getPaymentType())) {
            return updatePaymentStatus(payment, TransactionStatus.ACSC);
        }
        return payment.getFrequency().equals(FrequencyCode.DAILY)
                       ? checkDailyPayment(payment, userName)
                       : schedulePayment(payment);
    }


    private TransactionStatusBO checkDailyPayment(Payment payment, String userName) {
        return payment.getExecutionRule().equals(PRECEDING)
                       ? precedingExecution(payment, userName, payment.getStartDate().withDayOfMonth(payment.getDayOfExecution()))
                       : followingExecution(payment, userName, payment.getStartDate().withDayOfMonth(payment.getDayOfExecution()));
    }

    private TransactionStatusBO followingExecution(Payment payment, String userName, LocalDate dayOfExecution) {
        if (dateCalculator(PRECEDING, LocalDate.now()).isNonWorkingDay(LocalDate.now().minusDays(1)) && !dateCalculator(PRECEDING, LocalDate.now()).isNonWorkingDay(dayOfExecution)) {
            LocalDate prevBusinessDay = dateCalculator(PRECEDING, LocalDate.now().minusDays(1)).getCurrentBusinessDate();
            IntStream.range(prevBusinessDay.getDayOfMonth(), LocalDate.now().getDayOfMonth() - 1).forEach(i -> txService.bookPayment(payment, LocalDateTime.now(), userName));
        }
        return schedulePayment(payment);
    }

    private TransactionStatusBO precedingExecution(Payment payment, String userName, LocalDate dayOfExecution) {
        LocalDate nextBusinessDay = dateCalculator(FOLLOWING, LocalDate.now().plusDays(1)).getCurrentBusinessDate();
        if (dateCalculator(FOLLOWING, LocalDate.now()).isNonWorkingDay(LocalDate.now().plusDays(1)) && !dateCalculator(FOLLOWING, LocalDate.now()).isNonWorkingDay(dayOfExecution)) {
            IntStream.range(LocalDate.now().getDayOfMonth() + 1, nextBusinessDay.getDayOfMonth()).forEach(i -> txService.bookPayment(payment, LocalDateTime.now(), userName));
        }
        payment.setExecutedDate(LocalDateTime.of(nextBusinessDay.minusDays(1), LocalTime.MIN));
        return schedulePayment(payment);
    }

    private TransactionStatusBO updatePaymentStatus(Payment payment, TransactionStatus status) {
        payment.setTransactionStatus(status);
        payment.setNextScheduledExecution(null);
        paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(status.name());
    }

    public TransactionStatusBO schedulePayment(Payment payment) {
        LocalDate executionDate = calculateExecutionDate(payment);
        TransactionStatus status = executionDate == null
                                           ? TransactionStatus.ACSC
                                           : TransactionStatus.ACSP;
        payment.setTransactionStatus(status);
        LocalDateTime executionDateTime = null;
        if (executionDate != null) {
            LocalTime executionTime = payment.getRequestedExecutionTime() == null
                                              ? LocalTime.MIN
                                              : payment.getRequestedExecutionTime();
            executionDateTime = LocalDateTime.of(executionDate, executionTime);
        }
        payment.setNextScheduledExecution(executionDateTime);
        Payment savedPayment = paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(savedPayment.getTransactionStatus().name());
    }

    public AmountBO calculateTotalPaymentAmount(PaymentBO payment) {
        return payment.getTargets().stream()
                       .map(PaymentTargetBO::getInstructedAmount)
                       .reduce((left, right) -> new AmountBO(Currency.getInstance("EUR"), left.getAmount().add(right.getAmount())))
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(PAYMENT_PROCESSING_FAILURE)
                                                  .devMsg(String.format("Could not calculate total amount for payment: %s.", payment.getPaymentId()))
                                                  .build());
    }

    private LocalDate calculateExecutionDate(Payment payment) {
        LocalDate date = payment.getPaymentType() == PaymentType.PERIODIC
                                 ? calculateForPeriodicPmt(payment)
                                 : calculateForRegularPmt(payment);

        return payment.isLastExecuted(date)
                       ? null
                       : dateCalculator(payment.getExecutionRule(), date).getCurrentBusinessDate();
    }

    private LocalDate calculateForRegularPmt(Payment payment) {
        return payment.getRequestedExecutionDate() != null && !payment.getRequestedExecutionDate().isBefore(LocalDate.now())
                       ? payment.getRequestedExecutionDate()
                       : LocalDate.now();
    }

    private LocalDate calculateForPeriodicPmt(Payment payment) {
        return payment.getExecutedDate() == null
                       ? nextDayOfExecution(payment)
                       : ExecutionTimeHolder.getExecutionDate(payment);
    }

    private static LocalDate nextDayOfExecution(Payment payment) {
        if (payment.getStartDate().isAfter(payment.getStartDate().withDayOfMonth(payment.getDayOfExecution()))) {
            return payment.getStartDate();
        }
        return payment.getStartDate().withDayOfMonth(payment.getDayOfExecution());
    }

    private DateCalculator<LocalDate> dateCalculator(String executionRule, LocalDate date) {
        DateCalculator<LocalDate> calc = PRECEDING.equals(executionRule)
                                                 ? LocalDateKitCalculatorsFactory.backwardCalculator(CALENDAR_NAME)
                                                 : LocalDateKitCalculatorsFactory.forwardCalculator(CALENDAR_NAME);
        return calc.setStartDate(date);
    }

    private static List<LocalDate> readHolidays() {
        try {
            return YamlReader.getInstance().getListFromFile("holidays.yml", LocalDate.class);
        } catch (IOException e) {
            throw DepositModuleException.builder().errorCode(PAYMENT_PROCESSING_FAILURE).devMsg(e.getMessage()).build();
        }
    }
}
