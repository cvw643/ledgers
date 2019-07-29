package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.core.UpdatePassword;

public interface UpdatePasswordService {
    UpdatePassword updatePassword(String userId, String newPassword);
}
