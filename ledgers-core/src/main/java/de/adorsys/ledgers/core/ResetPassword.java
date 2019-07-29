package de.adorsys.ledgers.core;

import lombok.Data;

@Data
public class ResetPassword {
    private String login;
    private String email;
    private String phone;
    private String code;
    private String newPassword;

    public ResetPassword withCode(String code) {
        this.code = code;
        return this;
    }
}
