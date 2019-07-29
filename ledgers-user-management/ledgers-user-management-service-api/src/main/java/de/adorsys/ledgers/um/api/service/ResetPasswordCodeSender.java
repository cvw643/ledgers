package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.core.ResetPassword;
import de.adorsys.ledgers.core.SendCode;

public interface ResetPasswordCodeSender {
     SendCode sendCode(ResetPassword source);
}
