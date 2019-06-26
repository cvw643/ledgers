package de.adorsys.ledgers.postings.api.exception;

import lombok.Data;

@Data
public class PostingModuleException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    private final String devMsg;
}
