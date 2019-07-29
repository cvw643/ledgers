package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.core.VerifyCode;

public interface ResetPasswordCodeVerifier {
    VerifyCode verifyCode(String code);
}
