package com.tharun.currency_alert_service;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAlertRequest(
    String currencyPair,
    String direction,
    BigDecimal threshold
) {
}
