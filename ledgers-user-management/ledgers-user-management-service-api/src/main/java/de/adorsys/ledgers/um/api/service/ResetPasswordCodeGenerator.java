package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.core.GenerateCode;
import de.adorsys.ledgers.core.ResetPassword;

public interface ResetPasswordCodeGenerator {
    GenerateCode generateCode(ResetPassword source);
}
