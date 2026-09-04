package com.tharun.currencyalertservice.dto;

import java.math.BigDecimal;

public record CreateAlertRequest(
        String currencyPair,
        String direction,
        BigDecimal threshold
) {
}