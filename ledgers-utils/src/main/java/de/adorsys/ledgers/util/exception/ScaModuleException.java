package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScaModuleException extends RuntimeException {
    private final SCAErrorCode errorCode;
    private final String devMsg;
}
