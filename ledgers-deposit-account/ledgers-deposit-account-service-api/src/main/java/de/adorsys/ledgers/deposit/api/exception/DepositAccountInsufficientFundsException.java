package de.adorsys.ledgers.deposit.api.exception;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;

public class DepositAccountInsufficientFundsException extends RuntimeException {
    private static final String MESSAGE = "Payment with id=%s rejected due to insufficient funds";
    private PaymentBO payment;

    public DepositAccountInsufficientFundsException(PaymentBO payment) {
        super(String.format(MESSAGE, payment.getPaymentId()));
        this.payment=payment;
    }

    public PaymentBO getPayment() {
        return payment;
    }
}
