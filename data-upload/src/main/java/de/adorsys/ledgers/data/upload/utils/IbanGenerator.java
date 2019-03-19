package de.adorsys.ledgers.data.upload.utils;

import java.math.BigInteger;

public class IbanGenerator {
    private final static String bankCode = "76070024";

    public static String generateIban(String tppCode, String bban) {
        if (isDigitsAndSize(tppCode, 8) && isDigitsAndSize(bban, 2)) {
            BigInteger totalNr = new BigInteger(bankCode + tppCode + bban + "131400");
            String checkSum = String.valueOf(98 - totalNr.remainder(new BigInteger("97")).intValue());
            if (checkSum.length() < 2) {
                checkSum = "0" + checkSum;
            }
            return "DE" + checkSum + bankCode + tppCode + bban;
        }
        throw new IllegalArgumentException("Inappropriate data for IBAN creation" + " " + tppCode + " " + bban);
    }

    private static boolean isDigitsAndSize(String toCheck, int size) {
        String regex = "\\d+";
        return toCheck.matches(regex) && toCheck.length() == size;
    }
}
