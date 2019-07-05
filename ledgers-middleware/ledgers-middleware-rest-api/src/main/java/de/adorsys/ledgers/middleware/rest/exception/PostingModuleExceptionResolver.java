package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.postings.api.exception.PostingModuleErrorCode;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;

import static de.adorsys.ledgers.postings.api.exception.PostingModuleErrorCode.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class PostingModuleExceptionResolver {
    private static final EnumMap<PostingModuleErrorCode, HttpStatus> httpStatusMap = new EnumMap<>(PostingModuleErrorCode.class);

    static {
        //404 Block
        httpStatusMap.put(LEDGER_ACCOUNT_NOT_FOUND, NOT_FOUND);
        httpStatusMap.put(LEDGER_NOT_FOUND, NOT_FOUND);
        httpStatusMap.put(POSTING_NOT_FOUND, NOT_FOUND);
        httpStatusMap.put(CHART_OF_ACCOUNT_NOT_FOUND, NOT_FOUND);

        //400 Block
        httpStatusMap.put(DOBLE_ENTRY_ERROR, BAD_REQUEST);
        httpStatusMap.put(BASE_LINE_TIME_ERROR, BAD_REQUEST);
        httpStatusMap.put(POSTING_TIME_MISSING, BAD_REQUEST);
        httpStatusMap.put(NOT_ENOUGH_INFO, BAD_REQUEST);
        httpStatusMap.put(NO_CATEGORY, BAD_REQUEST);
    }

    public static HttpStatus getHttpStatus(PostingModuleException e) {
        return httpStatusMap.get(e.getPostingModuleErrorCode());
    }
}
