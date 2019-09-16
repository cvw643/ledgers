package de.adorsys.ledgers.deposit.api.domain;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PaymentBOTest {
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    public void isValidAmount() {
        PaymentBO given = getPayment(true);
        boolean valid = given.isValidAmount();
        assertThat(valid).isTrue();
    }

    @Test
    public void isValidAmount_negative() {
        PaymentBO given = getPayment(false);
        boolean valid = given.isValidAmount();
        assertThat(valid).isFalse();
    }

    private PaymentBO getPayment(boolean isPositiveAmount) {
        PaymentTargetBO target1 = new PaymentTargetBO();
        PaymentTargetBO target2 = new PaymentTargetBO();
        target1.setInstructedAmount(new AmountBO(EUR, BigDecimal.ONE));
        target2.setInstructedAmount(new AmountBO(EUR, BigDecimal.valueOf(isPositiveAmount ? 1 : 0)));
        PaymentBO payment = new PaymentBO();
        payment.setTargets(Arrays.asList(target1, target2));
        return payment;
    }
}
