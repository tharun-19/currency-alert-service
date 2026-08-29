package com.tharun.currency_alert_service;

import java.math.BigDecimal;

public record RateFetchedEvent(String currencyPair, BigDecimal rate, String timestamp) {
}
