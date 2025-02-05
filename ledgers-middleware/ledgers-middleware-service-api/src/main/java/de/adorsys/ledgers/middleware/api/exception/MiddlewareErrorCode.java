package de.adorsys.ledgers.middleware.api.exception;

public enum MiddlewareErrorCode {
    CURRENCY_MISMATCH,
    NO_SUCH_ALGORITHM,
    PAYMENT_PROCESSING_FAILURE,
    AUTHENTICATION_FAILURE,
    ACCOUNT_CREATION_VALIDATION_FAILURE,
    INSUFFICIENT_PERMISSION,
    REQUEST_VALIDATION_FAILURE,
    CAN_NOT_RESOLVE_SCA_CHALLENGE_DATA,
    BRANCH_NOT_FOUND
}
