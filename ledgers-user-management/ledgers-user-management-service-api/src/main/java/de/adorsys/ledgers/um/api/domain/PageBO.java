package de.adorsys.ledgers.um.api.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageBO<T> {
    private int number;
    private int size;
    private long totalPages;
    private long totalElements;
    private List<T> content;
}
