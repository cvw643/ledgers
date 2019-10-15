package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.*;

import java.util.List;

@Data
@Builder
public class PageTO<T> {
    private int number;
    private int size;
    private long totalPages;
    private long totalElements;
    private List<T> content;
}
