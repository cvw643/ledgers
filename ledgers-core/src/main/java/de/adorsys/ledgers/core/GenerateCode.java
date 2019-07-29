package de.adorsys.ledgers.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerateCode {
    private String code;
    private boolean generated;

    public GenerateCode(String code) {
        this(code, true);
    }
}
