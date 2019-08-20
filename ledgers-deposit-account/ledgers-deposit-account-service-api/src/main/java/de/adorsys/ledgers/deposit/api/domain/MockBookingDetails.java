package de.adorsys.ledgers.deposit.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@Data
public class MockBookingDetails {
    String userAccount;
    LocalDate bookingDate;
    LocalDate valueDate;
    String remittance;
    String crDrName;
    String otherAccount;
    BigDecimal amount;
    Currency currency;

    @JsonIgnore
    public boolean isCreditTransaction() {
        return BigDecimal.ZERO.compareTo(amount) < 0;
    }
}
