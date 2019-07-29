package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.core.ResetPassword;
import de.adorsys.ledgers.core.SendCode;
import de.adorsys.ledgers.core.UpdatePassword;

public interface MiddlewareResetPasswordService {
    SendCode sendCode(ResetPassword resetPassword);

    UpdatePassword updatePassword(ResetPassword resetPassword);
}
